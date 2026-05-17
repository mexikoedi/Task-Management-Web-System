package io.github.mexikoedi.tmws.service;

import io.github.mexikoedi.tmws.dto.LoginRequest;
import io.github.mexikoedi.tmws.dto.PasswordResetRequest;
import io.github.mexikoedi.tmws.dto.RegisterRequest;
import io.github.mexikoedi.tmws.exception.EmailAlreadyExistsException;
import io.github.mexikoedi.tmws.exception.InvalidPasswordException;
import io.github.mexikoedi.tmws.exception.ResourceNotFoundException;
import io.github.mexikoedi.tmws.exception.UserDeactivatedException;
import io.github.mexikoedi.tmws.model.*;
import io.github.mexikoedi.tmws.repository.*;
import io.github.mexikoedi.tmws.util.JwtTokenProvider;
import io.github.mexikoedi.tmws.util.PasswordValidator;
import io.github.mexikoedi.tmws.util.TokenGenerator;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
  private final UserRepository userRepository;
  private final BoardRepository boardRepository;
  private final TaskRepository taskRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final EmailService emailService;
  private final SimpMessagingTemplate messagingTemplate;

  public AuthenticationService(
    UserRepository userRepository,
    BoardRepository boardRepository,
    TaskRepository taskRepository,
    VerificationTokenRepository verificationTokenRepository,
    PasswordResetTokenRepository passwordResetTokenRepository,
    PasswordEncoder passwordEncoder,
    JwtTokenProvider jwtTokenProvider,
    EmailService emailService,
    SimpMessagingTemplate messagingTemplate) {
    this.userRepository = userRepository;
    this.boardRepository = boardRepository;
    this.taskRepository = taskRepository;
    this.verificationTokenRepository = verificationTokenRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
    this.emailService = emailService;
    this.messagingTemplate = messagingTemplate;
  }

  /** Login mit Email und Passwort */
  public String login(LoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Benutzer mit E-Mail " + request.getEmail() + " nicht gefunden"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new InvalidPasswordException("Ungültige E-Mail oder ungültiges Passwort");
    }

    if (!user.isEnabled()) {
      throw new InvalidPasswordException(
          "Konto ist nicht aktiviert. Bitte überprüfen Sie Ihre E-Mail.");
    }

    // Token-Version erhöhen → alte Tabs werden ungültig
    user.setTokenVersion(user.getTokenVersion() + 1);
    userRepository.save(user);
    System.out.println("User " + request.getEmail() + " logged in successfully");
    return jwtTokenProvider.generateToken(user);
  }

  /** Registrierung mit Name, Email und Passwort */
  public String register(RegisterRequest request) {
    // Prüfe ob Email bereits existiert
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new EmailAlreadyExistsException(
          "E-Mail " + request.getEmail() + " ist bereits registriert");
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
    return "Registrierung erfolgreich. Bitte überprüfen Sie Ihre E-Mail, um Ihr Konto zu"
        + " verifizieren.";
  }

  /** Passwort zurücksetzen (via Email) */
  public String requestPasswordReset(PasswordResetRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Benutzer mit E-Mail " + request.getEmail() + " nicht gefunden"));

    if (!user.isEnabled()) {
      throw new UserDeactivatedException(
          "Benutzer mit E-Mail " + request.getEmail() + " deaktiviert");
    }

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

    return "Link zum Zurücksetzen des Passworts wurde an Ihre E-Mail gesendet";
  }

  /** Email-Verifikationsemail mit Token verifizieren */
  public void verifyEmail(String token) {
    System.out.println("TOKEN RECEIVED: " + token);
    VerificationToken verificationToken =
        verificationTokenRepository
            .findByToken(token)
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

  /** Passwort mit Token zurücksetzen */
  public void resetPassword(String token, String newPassword) {
    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(token)
            .orElseThrow(
                () -> new ResourceNotFoundException("Passwort-Reset-Token nicht gefunden"));

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

  /** Hole einen User anhand der E-Mail */
  public User getUserByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Benutzer mit E-Mail " + email + " nicht gefunden"));
  }

  /** Update Profil-Informationen (Name, Email, Image, Passwort) */
  public User updateProfile(
      String currentEmail,
      String newName,
      String newEmail,
      String currentPassword,
      String newPassword,
      String newPasswordConfirm,
      String image) {

    User user = getUserByEmail(currentEmail);

    boolean emailChanged = false;

    // Name ändern
    if (newName != null && !newName.trim().isEmpty()) {
      user.setName(newName.trim());
    }

    // Profilbild ändern / löschen
    if (image != null) { // Prüfe nur auf null, nicht auf isEmpty
      if (image.isEmpty()) {
        user.setImage(null); // Bild löschen
      } else {
        user.setImage(image); // Bild aktualisieren
      }
    }

    // E-Mail ändern
    if (newEmail != null && !newEmail.trim().isEmpty() && !newEmail.equals(user.getEmail())) {

      if (userRepository.existsByEmail(newEmail)) {
        throw new EmailAlreadyExistsException("E-Mail " + newEmail + " ist bereits vergeben");
      }

      emailChanged = true;
      user.setEmail(newEmail);
      user.setEmailVerified(false);
      user.setEnabled(false);
      user.setEmailChanged(true); // Flag für Response

      // Token generieren & speichern
      String token = TokenGenerator.generateToken();
      VerificationToken verificationToken = new VerificationToken();
      verificationToken.setToken(token);
      verificationToken.setUser(user);
      verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

      userRepository.save(user);
      verificationTokenRepository.save(verificationToken);

      // E-Mail senden
      emailService.sendRegistrationEmail(
          newEmail, "http://localhost:4200/verify-email?token=" + token);
    } else {
      user.setEmailChanged(false);
    }

    // Passwort ändern
    boolean passwordChanged = false;
    if (newPassword != null && !newPassword.isEmpty()) {
      if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
        throw new InvalidPasswordException("Aktuelles Passwort ist falsch");
      }
      if (!newPassword.equals(newPasswordConfirm)) {
        throw new InvalidPasswordException("Neue Passwörter stimmen nicht überein");
      }
      if (!PasswordValidator.isValid(newPassword)) {
        throw new InvalidPasswordException(PasswordValidator.getPasswordRequirements());
      }
      user.setPassword(passwordEncoder.encode(newPassword));
      passwordChanged = true;

      userRepository.save(user);

      // Passwort-Änderungs-Mail optional
      emailService.sendPasswordChangedEmail(newEmail, user.getEmail());
    }

    User saved = userRepository.save(user);

    // Alle Mitglieder inklusive Owner benachrichtigen (damit Profilbild/Name Änderung bei jedem ankommt)
    for (User u: userRepository.findAll()) {
      notifyUser(u.getId());
    }

    // Alle Boards, in denen der User Mitglied ist, updaten
    List<Board> boards = boardRepository.findAllByMembersContains(saved);
    for (Board b : boards) {
      notifyBoard(b.getId());
    }
    return saved;
  }

  /** Ändere das Passwort des Users */
  public void changePassword(String email, String currentPassword, String newPassword) {
    User user = getUserByEmail(email);

    // Prüfe ob aktuelles Passwort korrekt ist
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new InvalidPasswordException("Aktuelles Passwort ist nicht korrekt");
    }

    // Validiere neues Passwort
    if (!PasswordValidator.isValid(newPassword)) {
      throw new InvalidPasswordException(PasswordValidator.getPasswordRequirements());
    }

    // Setze neues Passwort
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    System.out.println("Password changed for user " + email);
  }

  /** Deaktiviere den Account eines Users (für JWT-authentifizierte Anfrage) */
  @Transactional
  public void deactivateAccount(String email) {
    // 1. User laden
    User user =
        userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

    Long userId = user.getId();

    // Set aller betroffenen User (z.B. Admin, andere Mitglieder)
    Set<Long> affectedUserIds = new HashSet<>();

    // 2. Alle Boards finden, in denen der User Mitglied ist
    List<Board> boards = boardRepository.findAllByMembersContains(user);

    for (Board board : boards) {

      // Entfernen aus board_members
      board.getMembers().remove(user);

      boolean wasOwner = board.getOwner().getId().equals(userId);

      // Alle verbleibenden Mitglieder
      List<User> remainingMembers =
          board.getMembers().stream().filter(u -> !u.getId().equals(userId)).toList();

      if (remainingMembers.isEmpty()) {
        // Keine Mitglieder mehr → Board löschen
        boardRepository.delete(board);
        // Alle Nutzer informieren, dass Board verschwunden ist
        notifyBoard(board.getId());
        continue;
      }

      if (wasOwner) {
        // Neuer Owner = erstes verbleibendes Mitglied
        User newOwner = remainingMembers.get(0);
        board.setOwner(newOwner);

        // E‑Mail an neuen Owner
        emailService.sendNewProjectboardOwnerEmail(newOwner.getEmail(), board.getTitle());
      }

      // Alle verbleibenden Mitglieder merken
      for (User u : remainingMembers) {
        affectedUserIds.add(u.getId());
      }

      boardRepository.save(board);
      notifyBoard(board.getId());
    }

    // 3. User aus allen Task‑Assignees entfernen
    List<Task> tasks = taskRepository.findAllByAssigneesContains(user);

    for (Task task : tasks) {
      task.getAssignees().remove(user);
      taskRepository.save(task);
      // Board des Tasks informieren
      notifyBoard(task.getColumn().getBoard().getId());
    }

    // 4. Tokens löschen
    passwordResetTokenRepository.deleteAllByUser(user);
    verificationTokenRepository.deleteAllByUser(user);

    // 5. User deaktivieren (Soft Delete)
    user.setEnabled(false);
    userRepository.save(user);

    // 6. User selbst informieren (Logout)
    notifyUser(userId);

    // 7. Alle verbleibenden Nutzer informieren
    for (Long id : affectedUserIds) {
      notifyUser(id);
    }

    // 8. E‑Mail an User selbst
    emailService.sendAccountDeactivationEmail(user.getEmail(), user.getEmail());

    System.out.println("Deactivated user account and cleaned all relations: " + email);
  }

  /** Verifiziere Email des Users (Legacy-Methode für Kompatibilität) */
  public void verifyUserEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Benutzer mit E-Mail " + email + " nicht gefunden"));

    user.setEmailVerified(true);
    user.setEnabled(true);
    userRepository.save(user);

    System.out.println("Email verified for user " + email);
  }

  public String getEmailFromToken(String token) {
    return jwtTokenProvider.getClaims(token).getSubject();
  }

  private void notifyBoard(Long boardId) {
    messagingTemplate.convertAndSend("/topic/board/" + boardId, "update");
  }

  private void notifyUser(Long userId) {
    messagingTemplate.convertAndSend("/topic/user/" + userId, "update");
  }
}
