package io.github.mexikoedi.tmws.controller;

import io.github.mexikoedi.tmws.dto.AuthResponse;
import io.github.mexikoedi.tmws.dto.ChangePasswordRequest;
import io.github.mexikoedi.tmws.dto.LoginRequest;
import io.github.mexikoedi.tmws.dto.MessageResponse;
import io.github.mexikoedi.tmws.dto.PasswordResetRequest;
import io.github.mexikoedi.tmws.dto.RegisterRequest;
import io.github.mexikoedi.tmws.dto.UpdateProfileRequest;
import io.github.mexikoedi.tmws.dto.UserResponse;
import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.service.AuthenticationService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthenticationService authenticationService;

  public AuthController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  /** POST /api/auth/login Authentifiziere einen User mit Email und Passwort */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    System.out.println("Login attempt for email: " + request.getEmail());
    String token = authenticationService.login(request);
    return ResponseEntity.ok(new AuthResponse(token, "Anmeldung erfolgreich", true));
  }

  /** POST /api/auth/register Registriere einen neuen User */
  @PostMapping("/register")
  public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
    System.out.println("Registration attempt for email: " + request.getEmail());
    String message = authenticationService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(message, true));
  }

  /** POST /api/auth/password-reset Fordere einen Passwort-Reset an (via Email) */
  @PostMapping("/password-reset")
  public ResponseEntity<MessageResponse> requestPasswordReset(
      @Valid @RequestBody PasswordResetRequest request) {
    System.out.println("Password reset request for email: " + request.getEmail());
    String message = authenticationService.requestPasswordReset(request);
    return ResponseEntity.ok(new MessageResponse(message, true));
  }

  /** GET /api/auth/verify-email Verifiziere die Email eines Users mit Token */
  @GetMapping("/verify-email")
  public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
    System.out.println("Email verification with token");
    authenticationService.verifyEmail(token);
    return ResponseEntity.ok(new MessageResponse("E-Mail erfolgreich verifiziert", true));
  }

  /** PUT /api/auth/reset-password Passwort mit Token zurücksetzen */
  @PutMapping("/reset-password")
  public ResponseEntity<MessageResponse> resetPassword(
      @RequestParam String token, @RequestBody Map<String, String> request) {
    System.out.println("Password reset with token");
    String newPassword = request.get("password");
    authenticationService.resetPassword(token, newPassword);
    return ResponseEntity.ok(new MessageResponse("Passwort erfolgreich zurückgesetzt", true));
  }

  /** GET /api/auth/health Health Check für das Authentication Backend */
  @GetMapping("/health")
  public ResponseEntity<MessageResponse> health() {
    return ResponseEntity.ok(new MessageResponse("Authentifizierungsdienst läuft", true));
  }

  /** GET /api/auth/me Hol die Benutzerinformationen (aus JWT extrahiert) */
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser(@RequestParam String email) {
    User user = authenticationService.getUserByEmail(email);
    return ResponseEntity.ok(
        new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.isEmailVerified(),
            user.isEmailChanged(),
            user.getImage()));
  }

  /** PUT /api/auth/profile Aktualisiere Profilinformationen (Name) */
  @PutMapping("/profile")
  public ResponseEntity<UserResponse> updateProfile(
    @Valid @RequestBody UpdateProfileRequest request,
    @RequestHeader("Authorization") String authHeader) {

    String token = authHeader.replace("Bearer ", "");
    String email = authenticationService.getEmailFromToken(token);

    User user = authenticationService.updateProfile(
      email,
      request.getName(),
      request.getNewEmail(),
      request.getCurrentPassword(),
      request.getNewPassword(),
      request.getNewPasswordConfirm(),
      request.getImage()
    );

    return ResponseEntity.ok(
      new UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.isEmailVerified(),
        user.isEmailChanged(),
        user.getImage()
      )
    );
  }

  /** PUT /api/auth/change-password Ändere das Passwort */
  @PutMapping("/change-password")
  public ResponseEntity<MessageResponse> changePassword(
      @RequestParam String email,
      @Valid @RequestBody ChangePasswordRequest request) {
    // Prüfe ob neue Passwörter übereinstimmen
    if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Neue Passwörter stimmen nicht überein", false));
    }
    authenticationService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
    return ResponseEntity.ok(new MessageResponse("Passwort erfolgreich geändert", true));
  }

  /** DELETE /api/auth/me Deaktiviere den Account des aktuellen Users (über JWT im Authorization Header) */
  @DeleteMapping("/me")
  public ResponseEntity<?> deactivateAccount(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    String email = authenticationService.getEmailFromToken(token);
    authenticationService.deactivateAccount(email);
    return ResponseEntity.ok(new MessageResponse("Account erfolgreich deaktiviert", true));
  }
}
