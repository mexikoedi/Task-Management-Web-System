/**
 * Diese Klasse enthält Unit-Tests für die AuthService-Klasse, um sicherzustellen, dass die
 * Authentifizierungs- und Registrierungslogik korrekt funktioniert. Es werden verschiedene
 * Szenarien getestet, einschließlich erfolgreicher Anmeldungen, Registrierungen,
 * Passwortzurücksetzungen und E-Mail-Verifizierungen sowie Fehlerfälle wie ungültige Anmeldedaten,
 * bereits existierende E-Mails und abgelaufene oder bereits verwendete Tokens. Mockito wird
 * verwendet, um die Abhängigkeiten zu mocken und die Interaktionen zu überprüfen, während JUnit 5
 * als Testframework verwendet wird, um die Tests zu strukturieren und auszuführen. Die Tests decken
 * Funktionen wie das Anmelden, Registrieren, Anfordern von Passwortzurücksetzungen, Verifizieren
 * von E-Mails, Zurücksetzen von Passwörtern und Abrufen der aktuellen Benutzerdaten ab. Jeder Test
 * überprüft die erwarteten Ergebnisse und die Interaktionen mit den gemockten Abhängigkeiten, um
 * sicherzustellen, dass die AuthService-Klasse wie erwartet funktioniert.
 */
package io.github.mexikoedi.tmws.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.mexikoedi.tmws.dto.*;
import io.github.mexikoedi.tmws.exception.*;
import io.github.mexikoedi.tmws.model.*;
import io.github.mexikoedi.tmws.repository.*;
import io.github.mexikoedi.tmws.security.JwtProvider;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private VerificationTokenRepository verificationTokenRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtProvider jwtProvider;
  @Mock private EmailService emailService;
  @Mock private WebSocketNotificationService websocket;
  @Mock private BoardRepository boardRepository;
  @InjectMocks private AuthService authService;
  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setPassword("encodedPass");
    user.setEnabled(true);
    user.setEmailVerified(true);
    user.setTokenVersion(1);
  }

  @Test
  @DisplayName(
      "login() - Sollte bei gültigen Anmeldedaten ein JWT zurückgeben und die Token-Version"
          + " erhöhen.")
  void login_success() {
    LoginRequest req = new LoginRequest("test@example.com", "123");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("123", "encodedPass")).thenReturn(true);
    when(jwtProvider.generateToken(user)).thenReturn("JWT");
    String token = authService.login(req);
    assertEquals("JWT", token);
    assertEquals(2, user.getTokenVersion());
    verify(userRepository).save(user);
    verify(websocket).sendForceLogout(1L, 2);
  }

  @Test
  @DisplayName(
      "login() - Sollte eine ResourceNotFoundException werfen, wenn der Benutzer nicht gefunden"
          + " wird.")
  void login_userNotFound() {
    when(userRepository.findByEmail("x")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> authService.login(new LoginRequest("x", "y")));
  }

  @Test
  @DisplayName(
      "login() - Sollte eine InvalidPasswordException werfen, wenn das Passwort ungültig ist.")
  void login_invalidPassword() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(any(), any())).thenReturn(false);
    assertThrows(
        InvalidPasswordException.class,
        () -> authService.login(new LoginRequest("test@example.com", "wrong")));
  }

  @Test
  @DisplayName(
      "login() - Sollte eine UserDeactivatedException werfen, wenn der Benutzer deaktiviert ist.")
  void login_userDisabled() {
    user.setEnabled(false);
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(any(), any())).thenReturn(true);
    assertThrows(
        UserDeactivatedException.class,
        () -> authService.login(new LoginRequest("test@example.com", "123")));
  }

  @Test
  @DisplayName(
      "register() - Sollte einen neuen Benutzer registrieren, einen Verifizierungs-Token erstellen"
          + " und eine E-Mail senden.")
  void register_success() {
    RegisterRequest req = new RegisterRequest("User", "new@example.com", "123");
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(passwordEncoder.encode("123")).thenReturn("ENC");
    String msg = authService.register(req);
    verify(userRepository).save(any(User.class));
    verify(verificationTokenRepository).save(any(VerificationToken.class));
    verify(emailService).sendRegistrationEmail(eq("new@example.com"), contains("verify-email"));
    assertTrue(msg.contains("Registrierung erfolgreich"));
  }

  @Test
  @DisplayName(
      "register() - Sollte eine EmailAlreadyExistsException werfen, wenn die E-Mail bereits"
          + " existiert.")
  void register_emailExists() {
    when(userRepository.existsByEmail(anyString())).thenReturn(true);
    assertThrows(
        EmailAlreadyExistsException.class,
        () -> authService.register(new RegisterRequest("x", "y", "z")));
  }

  @Test
  @DisplayName(
      "requestPasswordReset() - Sollte einen Passwort-Zurücksetzen-Token erstellen und eine E-Mail"
          + " senden, wenn die E-Mail existiert und der Benutzer aktiviert ist.")
  void requestPasswordReset_success() {
    PasswordResetInquiryRequest req = new PasswordResetInquiryRequest("test@example.com");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    String msg = authService.requestPasswordReset(req);
    verify(passwordResetTokenRepository).save(any());
    verify(emailService).sendPasswordResetEmail(eq("test@example.com"), contains("reset-password"));
    assertTrue(msg.contains("Link zum Zurücksetzen"));
  }

  @Test
  @DisplayName(
      "requestPasswordReset() - Sollte eine UserDeactivatedException werfen, wenn der Benutzer"
          + " deaktiviert ist.")
  void requestPasswordReset_userDisabled() {
    user.setEnabled(false);
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    assertThrows(
        UserDeactivatedException.class,
        () ->
            authService.requestPasswordReset(new PasswordResetInquiryRequest("test@example.com")));
  }

  @Test
  @DisplayName(
      "verifyEmail() - Sollte die E-Mail-Adresse verifizieren, den Benutzer aktivieren und den"
          + " Token als verwendet markieren, wenn der Token gültig ist.")
  void verifyEmail_success() {
    VerificationToken token = new VerificationToken();
    token.setUser(user);
    token.setExpiryDate(LocalDateTime.now().plusHours(1));
    token.setUsed(false);
    when(verificationTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
    when(boardRepository.findBoardsForUserWithRelations(user)).thenReturn(Collections.emptyList());
    authService.verifyEmail("abc");
    assertTrue(user.isEnabled());
    assertTrue(user.isEmailVerified());
    assertTrue(token.isUsed());
    verify(userRepository).save(user);
    verify(verificationTokenRepository).save(token);
  }

  @Test
  @DisplayName(
      "verifyEmail() - Sollte eine VerificationTokenExpiredException werfen, wenn der Token"
          + " abgelaufen ist.")
  void verifyEmail_expired() {
    VerificationToken token = new VerificationToken();
    token.setExpiryDate(LocalDateTime.now().minusHours(1));
    when(verificationTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
    assertThrows(VerificationTokenExpiredException.class, () -> authService.verifyEmail("abc"));
  }

  @Test
  @DisplayName(
      "verifyEmail() - Sollte eine VerificationTokenAlreadyUsedException werfen, wenn der Token"
          + " bereits verwendet wurde.")
  void verifyEmail_alreadyUsed() {
    VerificationToken token = new VerificationToken();
    token.setExpiryDate(LocalDateTime.now().plusHours(1));
    token.setUsed(true);
    when(verificationTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
    assertThrows(VerificationTokenAlreadyUsedException.class, () -> authService.verifyEmail("abc"));
  }

  @Test
  @DisplayName(
      "resetPassword() - Sollte das Passwort zurücksetzen, wenn der Token gültig ist, und den Token"
          + " als verwendet markieren.")
  void resetPassword_success() {
    PasswordResetToken token = new PasswordResetToken();
    token.setUser(user);
    token.setExpiryDate(LocalDateTime.now().plusHours(1));
    token.setUsed(false);
    when(passwordResetTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
    when(passwordEncoder.encode("new")).thenReturn("ENC");
    authService.resetPassword("abc", new PasswordResetRequest("new"));
    assertEquals("ENC", user.getPassword());
    assertTrue(token.isUsed());
    verify(userRepository).save(user);
    verify(passwordResetTokenRepository).save(token);
  }

  @Test
  @DisplayName(
      "resetPassword() - Sollte eine PasswordResetTokenExpiredException werfen, wenn der Token"
          + " abgelaufen ist.")
  void resetPassword_expired() {
    PasswordResetToken token = new PasswordResetToken();
    token.setExpiryDate(LocalDateTime.now().minusHours(1));
    when(passwordResetTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
    assertThrows(
        PasswordResetTokenExpiredException.class,
        () -> authService.resetPassword("abc", new PasswordResetRequest("x")));
  }

  @Test
  @DisplayName(
      "resetPassword() - Sollte eine PasswordResetTokenAlreadyUsedException werfen, wenn der Token"
          + " bereits verwendet wurde.")
  void resetPassword_used() {
    PasswordResetToken token = new PasswordResetToken();
    token.setExpiryDate(LocalDateTime.now().plusHours(1));
    token.setUsed(true);
    when(passwordResetTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
    assertThrows(
        PasswordResetTokenAlreadyUsedException.class,
        () -> authService.resetPassword("abc", new PasswordResetRequest("x")));
  }

  @Test
  @DisplayName(
      "getCurrentUser() - Sollte die Benutzerdaten zurückgeben, wenn der Benutzer gefunden und"
          + " aktiviert ist.")
  void getCurrentUser_success() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    UserResponse res = authService.getCurrentUser("test@example.com");
    assertEquals("test@example.com", res.getEmail());
  }

  @Test
  @DisplayName(
      "getCurrentUser() - Sollte eine UserDeactivatedException werfen, wenn der Benutzer"
          + " deaktiviert ist.")
  void getCurrentUser_disabled() {
    user.setEnabled(false);
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    assertThrows(
        UserDeactivatedException.class, () -> authService.getCurrentUser("test@example.com"));
  }

  @Test
  @DisplayName(
      "getCurrentUser() - Sollte eine ResourceNotFoundException werfen, wenn der Benutzer nicht"
          + " gefunden wird.")
  void getCurrentUser_notFound() {
    when(userRepository.findByEmail("x")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser("x"));
  }
}
