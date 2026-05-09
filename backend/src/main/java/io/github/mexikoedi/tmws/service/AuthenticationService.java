package io.github.mexikoedi.tmws.service;

import io.github.mexikoedi.tmws.dto.LoginRequest;
import io.github.mexikoedi.tmws.dto.RegisterRequest;
import io.github.mexikoedi.tmws.dto.PasswordResetRequest;
import io.github.mexikoedi.tmws.exception.EmailAlreadyExistsException;
import io.github.mexikoedi.tmws.exception.InvalidPasswordException;
import io.github.mexikoedi.tmws.exception.ResourceNotFoundException;
import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.model.VerificationToken;
import io.github.mexikoedi.tmws.model.PasswordResetToken;
import io.github.mexikoedi.tmws.repository.UserRepository;
import io.github.mexikoedi.tmws.repository.VerificationTokenRepository;
import io.github.mexikoedi.tmws.repository.PasswordResetTokenRepository;
import io.github.mexikoedi.tmws.util.JwtTokenProvider;
import io.github.mexikoedi.tmws.util.PasswordValidator;
import io.github.mexikoedi.tmws.util.TokenGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }

    /**
     * Login mit Email und Passwort
     */
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Benutzer mit E-Mail " + request.getEmail() + " nicht gefunden"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Ungültige E-Mail oder ungültiges Passwort");
        }

        if (!user.isEnabled()) {
            throw new InvalidPasswordException("Konto ist nicht aktiviert. Bitte überprüfen Sie Ihre E-Mail.");
        }

        System.out.println("User " + request.getEmail() + " logged in successfully");
        return jwtTokenProvider.generateToken(request.getEmail());
    }

    /**
     * Registrierung mit Name, Email und Passwort
     */
    public String register(RegisterRequest request) {
        // Prüfe ob Email bereits existiert
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("E-Mail " + request.getEmail() + " ist bereits registriert");
        }

        // Validiere Passwort
        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new InvalidPasswordException(PasswordValidator.getPasswordRequirements());
        }

        // Prüfe ob Passwörter übereinstimmen
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new InvalidPasswordException("Passwörter stimmen nicht überein");
        }

        // Erstelle neuen User
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setEmailVerified(false);

        userRepository.save(user);

        // Erstelle Verification Token (24 Stunden gültig)
        String token = TokenGenerator.generateToken();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        // Sende Bestätigungsemail mit Token
        String verificationLink = "http://localhost:4200/verify-email?token=" + token;
        emailService.sendRegistrationEmail(request.getEmail(), verificationLink);

        System.out.println("User " + request.getEmail() + " registered successfully");
        return "Registrierung erfolgreich. Bitte überprüfen Sie Ihre E-Mail, um Ihr Konto zu verifizieren.";
    }

    /**
     * Passwort zurücksetzen (via Email)
     */
    public String requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Benutzer mit E-Mail " + request.getEmail() + " nicht gefunden"));

        // Erstelle Password Reset Token (1 Stunde gültig)
        String token = TokenGenerator.generateToken();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(resetToken);

        // Sende Reset-Email mit Token
        String resetLink = "http://localhost:4200/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(request.getEmail(), resetLink);

        System.out.println("Password reset email sent to " + request.getEmail());
        return "Link zum Zurücksetzen des Passworts wurde an Ihre E-Mail gesendet";
    }

    /**
     * Email-Verifikationsemail mit Token verifizieren
     */
    public void verifyEmail(String token) {
        System.out.println("TOKEN RECEIVED: " + token);
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Verifikationstoken nicht gefunden"));

        if (verificationToken.isExpired()) {
            throw new InvalidPasswordException("Verifikationstoken ist abgelaufen");
        }

        if (verificationToken.isUsed()) {
            throw new InvalidPasswordException("Verifikationstoken wurde bereits verwendet");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        System.out.println("Email verified for user " + user.getEmail());
    }

    /**
     * Passwort mit Token zurücksetzen
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Passwort-Reset-Token nicht gefunden"));

        if (resetToken.isExpired()) {
            throw new InvalidPasswordException("Passwort-Reset-Token ist abgelaufen");
        }

        if (resetToken.isUsed()) {
            throw new InvalidPasswordException("Passwort-Reset-Token wurde bereits verwendet");
        }

        // Validiere neues Passwort
        if (!PasswordValidator.isValid(newPassword)) {
            throw new InvalidPasswordException(PasswordValidator.getPasswordRequirements());
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        System.out.println("Password reset for user " + user.getEmail());
    }

    /**
     * Verifiziere Email des Users (Legacy-Methode für Kompatibilität)
     */
    public void verifyUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Benutzer mit E-Mail " + email + " nicht gefunden"));

        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        System.out.println("Email verified for user " + email);
    }
}


