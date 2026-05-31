/**
 * Diese Klasse ist der REST-Controller für die Authentifizierungs- und Benutzerverwaltungsendpunkte der TMWS-Anwendung.
 */
package io.github.mexikoedi.tmws.controller;

import io.github.mexikoedi.tmws.dto.*;
import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.service.AuthService;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  /**
   * Konstruktor für AuthController, der die AuthService-Instanz injiziert bekommt.
   *
   * @param authService Die Service-Instanz, die die Geschäftslogik für Authentifizierung und
   * Benutzerverwaltung enthält.
   */
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * POST /api/auth/login - Authentifiziere einen User und gib ein JWT zurück (E-Mail + Passwort im Request Body).
   *
   * @param request LoginRequest mit E-Mail und Passwort.
   * @return JWT im ApiResponse, wenn erfolgreich, sonst Fehlernachricht.
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
    String token = authService.login(request);

    return ResponseEntity.ok(new ApiResponse(token, "Anmeldung erfolgreich.", true));
  }

  /**
   * POST /api/auth/register - Registriere einen neuen User (Name, E-Mail, Passwort im Request Body).
   *
   * @param request RegisterRequest mit Name, E-Mail, Passwort und Passwortbestätigung.
   * @return Erfolgsmeldung, dass die Registrierung erfolgreich war und eine Verifizierungs-E-Mail verschickt wurde.
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
    String message = authService.register(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(null, message, true));
  }

  /**
   * POST /api/auth/password-reset - Fordere einen Passwort-Reset an, indem du die E-Mail des Users angibst,
   * der sein Passwort vergessen hat.
   *
   * @param request PasswordResetInquiryRequest mit der E-Mail des Users, der sein Passwort zurücksetzen möchte.
   * @return Erfolgsmeldung, dass die Anfrage erfolgreich war und eine E-Mail mit Anweisungen zum Zurücksetzen des Passworts verschickt wurde.
   */
  @PostMapping("/password-reset")
  public ResponseEntity<ApiResponse> requestPasswordReset(@Valid @RequestBody PasswordResetInquiryRequest request) {
    String message = authService.requestPasswordReset(request);

    return ResponseEntity.ok(new ApiResponse(null, message, true));
  }

  /**
   * GET /api/auth/verify-email - Verifiziere die E-Mail eines Users, indem du den Verifizierungs-Token angibst,
   * der per E-Mail verschickt wurde.
   *
   * @param token Der Verifizierungs-Token, der in der E-Mail enthalten ist.
   * @return Erfolgsmeldung, dass die E-Mail erfolgreich verifiziert wurde und der Account jetzt aktiv ist.
   */
  @GetMapping("/verify-email")
  public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
    authService.verifyEmail(token);

    return ResponseEntity.ok(new ApiResponse(null, "E-Mail erfolgreich verifiziert.", true));
  }

  /**
   * PUT /api/auth/reset-password - Setze das Passwort eines Users zurück, indem du den Reset-Token angibst,
   * der per E-Mail verschickt wurde, sowie das neue Passwort im Request Body.
   *
   * @param token Der Reset-Token, der in der E-Mail enthalten ist.
   * @param request PasswordResetRequest mit dem neuen Passwort und der Passwortbestätigung.
   * @return Erfolgsmeldung, dass das Passwort erfolgreich zurückgesetzt wurde und der User sich jetzt mit dem
   * neuen Passwort anmelden kann.
   */
  @PutMapping("/reset-password")
  public ResponseEntity<ApiResponse> resetPassword(@RequestParam String token, @Valid @RequestBody PasswordResetRequest request) {
    authService.resetPassword(token, request);

    return ResponseEntity.ok(new ApiResponse(null, "Passwort erfolgreich zurückgesetzt.", true));
  }

  /**
   * PUT /api/auth/profile - Aktualisiere die Profilinformationen des aktuellen Users (über JWT im Authorization Header)
   * indem du die neuen Informationen im Request Body angibst.
   *
   * @param request UpdateProfileRequest mit den neuen Profilinformationen (Name, E-Mail, Profilbild-URL).
   * @return Die aktualisierten Benutzerinformationen im UserResponse, wenn erfolgreich, sonst Fehlernachricht.
   */
  @PutMapping("/profile")
  public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
    String email = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    User updatedUser = authService.updateProfile(email, request.getName(), request.getNewEmail(), request.getImage());

    return ResponseEntity.ok(new UserResponse(updatedUser));
  }

  /**
   * PUT /api/auth/profile/change-password - Ändere das Passwort des aktuellen Users (über JWT im Authorization Header)
   * indem du das aktuelle Passwort, das neue Passwort und die Passwortbestätigung im Request Body angibst.
   *
   * @param request UpdatePasswordRequest mit dem aktuellen Passwort, dem neuen Passwort und der Passwortbestätigung.
   * @return Erfolgsmeldung, dass das Passwort erfolgreich geändert wurde und der User sich jetzt mit dem neuen
   * Passwort anmelden kann.
   */
  @PutMapping("/profile/change-password")
  public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody UpdatePasswordRequest request) {
    String email = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    authService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());

    return ResponseEntity.ok(new ApiResponse(null, "Passwort erfolgreich geändert.", true));
  }

  /** DELETE /api/auth/me - Deaktiviere den Account des aktuellen Users (über JWT im Authorization Header).
   *
   * @return Erfolgsmeldung, dass der Account erfolgreich deaktiviert wurde.
   */
  @DeleteMapping("/me")
  public ResponseEntity<ApiResponse> deactivateAccount() {
    String email = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    authService.deactivateAccount(email);

    return ResponseEntity.ok(new ApiResponse(null, "Account erfolgreich deaktiviert.", true));
  }

   /**
   * GET /api/auth/me - Hole die Profilinformationen des aktuellen Users (über JWT im Authorization Header).
   *
   * @return Die Profilinformationen des aktuellen Users im UserResponse, wenn erfolgreich, sonst Fehlernachricht.
   */
  @GetMapping("/me")
  public UserResponse getCurrentUser() {
    String email = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

    return authService.getCurrentUser(email);
  }
}
