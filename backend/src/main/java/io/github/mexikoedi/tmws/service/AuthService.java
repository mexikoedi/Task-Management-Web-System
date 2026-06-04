/** Diese Klasse bietet Authentifizierungs- und Benutzerverwaltungsdienste für TMWS. */
package io.github.mexikoedi.tmws.service;

import io.github.mexikoedi.tmws.model.*;
import io.github.mexikoedi.tmws.repository.*;
import io.github.mexikoedi.tmws.security.JwtProvider;
import io.github.mexikoedi.tmws.security.TokenGenerator;
import io.github.mexikoedi.tmws.service.dto.*;
import io.github.mexikoedi.tmws.service.exception.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final BoardRepository boardRepository;
  private final TaskRepository taskRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final EmailService emailService;
  private final WebSocketNotificationService websocket;

  /**
   * Konstruktor für AuthService, der alle benötigten Abhängigkeiten injiziert.
   *
   * @param userRepository Repository für Benutzeroperationen.
   * @param boardRepository Repository für Board-Operationen.
   * @param taskRepository Repository für Task-Operationen.
   * @param verificationTokenRepository Repository für Verifikationstoken-Operationen.
   * @param passwordResetTokenRepository Repository für Passwort-Reset-Token-Operationen.
   * @param passwordEncoder PasswordEncoder für die sichere Speicherung von Passwörtern.
   * @param jwtProvider JwtProvider für die Generierung von JWT-Tokens.
   * @param emailService EmailService für den Versand von E-Mails.
   * @param websocket WebSocketNotificationService für die Benachrichtigung von Clients über
   *     WebSockets.
   */
  public AuthService(
      UserRepository userRepository,
      BoardRepository boardRepository,
      TaskRepository taskRepository,
      VerificationTokenRepository verificationTokenRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      PasswordEncoder passwordEncoder,
      JwtProvider jwtProvider,
      EmailService emailService,
      WebSocketNotificationService websocket) {
    this.userRepository = userRepository;
    this.boardRepository = boardRepository;
    this.taskRepository = taskRepository;
    this.verificationTokenRepository = verificationTokenRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtProvider = jwtProvider;
    this.emailService = emailService;
    this.websocket = websocket;
  }

  /**
   * Authentifiziert einen Benutzer anhand seiner E-Mail-Adresse und seines Passworts.
   *
   * @param request LoginRequest-Objekt, das die E-Mail-Adresse und das Passwort des Benutzers
   *     enthält.
   * @throws ResourceNotFoundException Wenn kein Benutzer mit der angegebenen E-Mail-Adresse
   *     gefunden wird.
   * @throws InvalidPasswordException Wenn das angegebene Passwort nicht mit dem gespeicherten
   *     Passwort übereinstimmt.
   * @throws UserDeactivatedException Wenn der Benutzer deaktiviert ist.
   * @return Ein JWT-Token, das für die Authentifizierung bei zukünftigen Anfragen verwendet werden
   *     kann.
   */
  public String login(LoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden."));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new InvalidPasswordException("Ungültiges Passwort.");
    }

    if (!user.isEnabled()) {
      throw new UserDeactivatedException("Benutzer ist deaktiviert.");
    }

    user.setTokenVersion(user.getTokenVersion() + 1);
    userRepository.save(user);
    websocket.sendForceLogout(user.getId(), user.getTokenVersion());

    return jwtProvider.generateToken(user);
  }

  /**
   * Registriert einen neuen Benutzer, speichert ihn in der Datenbank und sendet eine
   * Verifikations-E-Mail.
   *
   * @param request RegisterRequest-Objekt, das den Namen, die E-Mail-Adresse und das Passwort des
   *     neuen Benutzers enthält.
   * @throws EmailAlreadyExistsException Wenn die angegebene E-Mail-Adresse bereits von einem
   *     anderen Benutzer verwendet wird.
   * @return Eine Erfolgsmeldung, die den Benutzer auffordert, seine E-Mail-Adresse zu überprüfen,
   *     um das Konto zu verifizieren.
   */
  public String register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new EmailAlreadyExistsException("E-Mail ist bereits registriert.");
    }

    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setEnabled(false);
    user.setEmailVerified(false);
    userRepository.save(user);
    String token = TokenGenerator.generateToken();
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(token);
    verificationToken.setUser(user);
    verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
    verificationTokenRepository.save(verificationToken);
    String verificationLink = "http://localhost:4200/verify-email?token=" + token;
    emailService.sendRegistrationEmail(request.getEmail(), verificationLink);

    return "Registrierung erfolgreich. Bitte überprüfen Sie Ihre E-Mail, um Ihr Konto zu"
        + " verifizieren.";
  }

  /**
   * Initiiert den Passwort-Zurücksetzen-Prozess, indem er einen Reset-Token generiert, diesen in
   * der Datenbank speichert und einen Link zum Zurücksetzen des Passworts an die E-Mail-Adresse des
   * Benutzers sendet.
   *
   * @param request PasswordResetInquiryRequest-Objekt, das die E-Mail-Adresse des Benutzers
   *     enthält, der sein Passwort zurücksetzen möchte.
   * @throws ResourceNotFoundException Wenn kein Benutzer mit der angegebenen E-Mail-Adresse
   *     gefunden wird.
   * @throws UserDeactivatedException Wenn der Benutzer deaktiviert ist.
   * @return Eine Erfolgsmeldung, die den Benutzer darüber informiert, dass ein Link zum
   *     Zurücksetzen des Passworts an seine E-Mail-Adresse gesendet wurde.
   */
  public String requestPasswordReset(PasswordResetInquiryRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden."));

    if (!user.isEnabled()) {
      throw new UserDeactivatedException("Benutzer ist deaktiviert.");
    }

    String token = TokenGenerator.generateToken();
    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setToken(token);
    resetToken.setUser(user);
    resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
    passwordResetTokenRepository.save(resetToken);
    String resetLink = "http://localhost:4200/reset-password?token=" + token;
    emailService.sendPasswordResetEmail(request.getEmail(), resetLink);

    return "Link zum Zurücksetzen des Passworts wurde an Ihre E-Mail gesendet.";
  }

  /**
   * Verifiziert die E-Mail eines Users anhand des Verifikationstokens. Prüft ob Token existiert, ob
   * er abgelaufen ist oder bereits verwendet wurde.
   *
   * @param token Verifikationstoken aus der E-Mail.
   * @throws ResourceNotFoundException Wenn Token nicht gefunden wird.
   * @throws VerificationTokenExpiredException Wenn Token abgelaufen ist.
   * @throws VerificationTokenAlreadyUsedException Wenn Token bereits verwendet wurde.
   */
  @Transactional
  public void verifyEmail(String token) {
    VerificationToken verificationToken =
        verificationTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("Verifikationstoken nicht gefunden."));
    boolean isExpired = LocalDateTime.now().isAfter(verificationToken.getExpiryDate());

    if (isExpired) {
      throw new VerificationTokenExpiredException("Verifikationstoken ist abgelaufen.");
    }

    if (verificationToken.isUsed()) {
      throw new VerificationTokenAlreadyUsedException(
          "Verifikationstoken wurde bereits verwendet.");
    }

    User user = verificationToken.getUser();
    user.setEmailVerified(true);
    user.setEnabled(true);
    userRepository.save(user);
    verificationToken.setUsed(true);
    verificationTokenRepository.save(verificationToken);
    List<Board> boards = boardRepository.findBoardsForUserWithRelations(user);

    for (Board b : boards) {
      websocket.notifyBoardAndMembers(b);
    }
  }

  /**
   * Setzt das Passwort eines Users zurück, indem es den Reset-Token überprüft und das neue Passwort
   * speichert.
   *
   * @param token Passwort-Reset-Token aus der E-Mail.
   * @param request PasswordResetRequest-Objekt, das das neue Passwort enthält.
   * @throws ResourceNotFoundException Wenn Token nicht gefunden wird.
   * @throws PasswordResetTokenExpiredException Wenn Token abgelaufen ist.
   * @throws PasswordResetTokenAlreadyUsedException Wenn Token bereits verwendet wurde.
   */
  @Transactional
  public void resetPassword(String token, PasswordResetRequest request) {
    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(token)
            .orElseThrow(
                () -> new ResourceNotFoundException("Passwort-Reset-Token nicht gefunden."));
    boolean isExpired = LocalDateTime.now().isAfter(resetToken.getExpiryDate());

    if (isExpired) {
      throw new PasswordResetTokenExpiredException("Passwort-Reset-Token ist abgelaufen.");
    }

    if (resetToken.isUsed()) {
      throw new PasswordResetTokenAlreadyUsedException(
          "Passwort-Reset-Token wurde bereits verwendet.");
    }

    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    userRepository.save(user);
    resetToken.setUsed(true);
    passwordResetTokenRepository.save(resetToken);
  }

  /**
   * Aktualisiert das Profil eines Users, einschließlich Name, E-Mail und Profilbild.
   *
   * @param currentEmail Die aktuelle E-Mail-Adresse des Users, um ihn zu identifizieren.
   * @param newName Der neue Name des Users.
   * @param newEmail Die neue E-Mail-Adresse des Users.
   * @param image Das neue Profilbild des Users.
   * @throws ResourceNotFoundException Wenn kein User mit der aktuellen E-Mail-Adresse gefunden
   *     wird.
   * @throws EmailAlreadyExistsException Wenn die neue E-Mail-Adresse bereits von einem anderen User
   *     verwendet wird.
   * @return Das aktualisierte User-Objekt.
   */
  @Transactional
  public User updateProfile(String currentEmail, String newName, String newEmail, String image) {
    User user =
        userRepository
            .findByEmail(currentEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden."));

    if (newName != null && !newName.isBlank()) {
      user.setName(newName.trim());
    }

    if (image != null) {
      if (image.isBlank()) {
        user.setImage(null);
      } else {
        user.setImage(image);
      }
    }

    if (newEmail != null && !newEmail.isBlank() && !newEmail.equals(user.getEmail())) {
      if (userRepository.existsByEmail(newEmail)) {
        throw new EmailAlreadyExistsException("E-Mail ist bereits vergeben.");
      }

      user.setEmail(newEmail);
      user.setEmailVerified(false);
      user.setEnabled(false);
      user.setEmailChanged(true);
      String token = TokenGenerator.generateToken();
      VerificationToken verificationToken = new VerificationToken();
      verificationToken.setToken(token);
      verificationToken.setUser(user);
      verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
      userRepository.save(user);
      verificationTokenRepository.save(verificationToken);
      emailService.sendRegistrationEmail(
          newEmail, "http://localhost:4200/verify-email?token=" + token);
    } else {
      user.setEmailChanged(false);
    }

    User saved = userRepository.save(user);
    List<Board> boards = boardRepository.findBoardsForUserWithRelations(saved);

    for (Board b : boards) {
      websocket.notifyBoardAndMembers(b);
    }

    return saved;
  }

  /**
   * Ändert das Passwort eines Users, indem es das aktuelle Passwort überprüft und das neue Passwort
   * speichert.
   *
   * @param email Die E-Mail-Adresse des Users, dessen Passwort geändert werden soll.
   * @param currentPassword Das aktuelle Passwort des Users, das überprüft wird, um sicherzustellen,
   *     dass der Benutzer berechtigt ist, das Passwort zu ändern.
   * @param newPassword Das neue Passwort, das für den User gespeichert werden soll.
   * @throws ResourceNotFoundException Wenn kein User mit der angegebenen E-Mail-Adresse gefunden
   *     wird.
   * @throws InvalidPasswordException Wenn das aktuelle Passwort nicht mit dem gespeicherten
   *     Passwort übereinstimmt.
   */
  public void changePassword(String email, String currentPassword, String newPassword) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden."));

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new InvalidPasswordException("Aktuelles Passwort ist falsch.");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    emailService.sendPasswordChangedEmail(user.getEmail(), user.getEmail());
  }

  /**
   * Deaktiviert das Konto eines Users anhand seiner E-Mail-Adresse.
   *
   * @param email Die E-Mail-Adresse des Users, dessen Konto deaktiviert werden soll.
   * @throws ResourceNotFoundException Wenn kein User mit der angegebenen E-Mail-Adresse gefunden
   *     wird.
   */
  @Transactional
  public void deactivateAccount(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden."));
    Long userId = user.getId();
    List<Board> boards = boardRepository.findBoardsForUserWithRelations(user);

    for (Board board : boards) {
      boolean wasOwner = board.getOwner().getId().equals(userId);
      board.getMembers().removeIf(u -> u.getId().equals(userId));
      List<User> remainingMembers = board.getMembers().stream().filter(User::isEnabled).toList();

      if (remainingMembers.isEmpty()) {
        if (wasOwner) {
          boardRepository.delete(board);
        } else {
          boardRepository.save(board);
        }

        continue;
      }

      if (wasOwner) {
        User newOwner = remainingMembers.getFirst();
        board.setOwner(newOwner);
        emailService.sendNewProjectboardOwnerEmail(newOwner.getEmail(), board.getTitle());
      }

      boardRepository.save(board);
    }

    List<Task> tasks = taskRepository.findAllTasksFromUser(user);

    for (Task task : tasks) {
      task.getAssignees().removeIf(u -> u.getId().equals(userId));
      taskRepository.save(task);
    }

    passwordResetTokenRepository.deleteAllByUser(user);
    verificationTokenRepository.deleteAllByUser(user);
    user.setEnabled(false);
    userRepository.save(user);

    for (Board board : boards) {
      websocket.notifyBoardAndMembers(board);
    }

    emailService.sendAccountDeactivationEmail(user.getEmail(), user.getEmail());
  }

  /**
   * Gibt die Informationen des aktuell angemeldeten Users zurück, basierend auf seiner
   * E-Mail-Adresse.
   *
   * @param email Die E-Mail-Adresse des aktuell angemeldeten Users.
   * @throws ResourceNotFoundException Wenn kein User mit der angegebenen E-Mail-Adresse gefunden
   *     wird.
   * @throws UserDeactivatedException Wenn der User deaktiviert ist.
   * @return Ein UserResponse-Objekt, das die Informationen des aktuell angemeldeten Users enthält.
   */
  public UserResponse getCurrentUser(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden."));

    if (!user.isEnabled()) {
      throw new UserDeactivatedException("Benutzer ist deaktiviert.");
    }

    return new UserResponse(user);
  }
}
