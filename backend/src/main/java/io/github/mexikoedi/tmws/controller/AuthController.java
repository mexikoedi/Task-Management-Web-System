package io.github.mexikoedi.tmws.controller;

import io.github.mexikoedi.tmws.dto.AuthResponse;
import io.github.mexikoedi.tmws.dto.LoginRequest;
import io.github.mexikoedi.tmws.dto.MessageResponse;
import io.github.mexikoedi.tmws.dto.PasswordResetRequest;
import io.github.mexikoedi.tmws.dto.RegisterRequest;
import io.github.mexikoedi.tmws.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * POST /api/auth/login
     * Authentifiziere einen User mit Email und Passwort
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("Login attempt for email: " + request.getEmail());
        String token = authenticationService.login(request);
        return ResponseEntity.ok(new AuthResponse(
                token,
                "Anmeldung erfolgreich",
                true
        ));
    }

    /**
     * POST /api/auth/register
     * Registriere einen neuen User
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("Registration attempt for email: " + request.getEmail());
        String message = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(
                message,
                true
        ));
    }

    /**
     * POST /api/auth/password-reset
     * Fordere einen Passwort-Reset an (via Email)
     */
    @PostMapping("/password-reset")
    public ResponseEntity<MessageResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        System.out.println("Password reset request for email: " + request.getEmail());
        String message = authenticationService.requestPasswordReset(request);
        return ResponseEntity.ok(new MessageResponse(
                message,
                true
        ));
    }

    /**
     * GET /api/auth/verify-email
     * Verifiziere die Email eines Users mit Token
     */
    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        System.out.println("Email verification with token");
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok(new MessageResponse(
                "E-Mail erfolgreich verifiziert",
                true
        ));
    }

    /**
     * PUT /api/auth/reset-password
     * Passwort mit Token zurücksetzen
     */
    @PutMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestParam String token,
            @RequestBody Map<String, String> request) {
        System.out.println("Password reset with token");
        String newPassword = request.get("password");
        authenticationService.resetPassword(token, newPassword);
        return ResponseEntity.ok(new MessageResponse(
                "Passwort erfolgreich zurückgesetzt",
                true
        ));
    }

    /**
     * GET /api/auth/health
     * Health Check für das Authentication Backend
     */
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(new MessageResponse(
                "Authentifizierungsdienst läuft",
                true
        ));
    }
}





