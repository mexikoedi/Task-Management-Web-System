/** Diese Klasse ist für den Versand von E-Mails im TMWS verantwortlich. */
package io.github.mexikoedi.tmws.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final JavaMailSender mailSender;
  private final Logger logger = Logger.getLogger(EmailService.class.getName());

  /**
   * Konstruktor für die EmailService-Klasse, der den JavaMailSender injiziert.
   *
   * @param mailSender Der JavaMailSender, der für den Versand von E-Mails verwendet wird.
   */
  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * Sendet eine Registrierungsbestätigung an die angegebene E-Mail-Adresse mit einem
   * Verifizierungslink.
   *
   * @param toEmail Die E-Mail-Adresse des Empfängers.
   * @param verificationLink Der Link, den der Benutzer anklicken muss, um seine Registrierung
   *     abzuschließen.
   */
  @Async
  public void sendRegistrationEmail(String toEmail, String verificationLink) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Registrierungsbestätigung");
      message.setText(buildRegistrationEmailBody(verificationLink));
      message.setFrom("noreply@tmws.local");
      mailSender.send(message);
    } catch (MailSendException e) {
      logger.log(Level.WARNING, "E-Mail konnte nicht versandt werden. Server offline?", e);
    }
  }

  /**
   * Sendet eine E-Mail zum Zurücksetzen des Passworts an die angegebene E-Mail-Adresse mit einem
   * Link zum Erstellen eines neuen Passworts.
   *
   * @param toEmail Die E-Mail-Adresse des Empfängers.
   * @param resetLink Der Link, den der Benutzer anklicken muss, um ein neues Passwort zu erstellen.
   */
  @Async
  public void sendPasswordResetEmail(String toEmail, String resetLink) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Passwort zurücksetzen");
      message.setText(buildPasswordResetEmailBody(resetLink));
      message.setFrom("noreply@tmws.local");
      mailSender.send(message);
    } catch (MailSendException e) {
      logger.log(Level.WARNING, "E-Mail konnte nicht versandt werden. Server offline?", e);
    }
  }

  /**
   * Sendet eine E-Mail, die den Benutzer darüber informiert, dass sein Passwort erfolgreich
   * geändert wurde.
   *
   * @param toEmail Die E-Mail-Adresse des Empfängers.
   * @param email Die E-Mail-Adresse des Accounts, dessen Passwort geändert wurde, damit der
   *     Benutzer überprüfen kann, ob es sich um seinen Account handelt.
   */
  @Async
  public void sendPasswordChangedEmail(String toEmail, String email) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Passwort geändert");
      message.setText(buildPasswordChangedEmailBody(email));
      message.setFrom("noreply@tmws.local");
      mailSender.send(message);
    } catch (MailSendException e) {
      logger.log(Level.WARNING, "E-Mail konnte nicht versandt werden. Server offline?", e);
    }
  }

  /**
   * Sendet eine E-Mail, die den Benutzer darüber informiert, dass sein Account erfolgreich
   * deaktiviert wurde.
   *
   * @param toEmail Die E-Mail-Adresse des Empfängers.
   * @param email Die E-Mail-Adresse des Accounts, der deaktiviert wurde, damit der Benutzer
   *     überprüfen kann, ob es sich um seinen Account handelt.
   */
  @Async
  public void sendAccountDeactivationEmail(String toEmail, String email) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Account deaktiviert");
      message.setText(buildAccountDeactivationEmailBody(email));
      message.setFrom("noreply@tmws.local");
      mailSender.send(message);
    } catch (MailSendException e) {
      logger.log(Level.WARNING, "E-Mail konnte nicht versandt werden. Server offline?", e);
    }
  }

  /**
   * Sendet eine E-Mail, die den Benutzer darüber informiert, dass sein Account zu einem
   * Projektboard eingeladen.
   *
   * @param toEmail Die E-Mail-Adresse des Empfängers.
   * @param projectBoard Der Name des Projektboards, zu dem der Benutzer eingeladen wurde, damit er
   *     überprüfen kann, ob es sich um ein Projektboard handelt, zu dem er Zugang haben sollte.
   */
  @Async
  public void sendAccountInvitedEmail(String toEmail, String projectBoard) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Account zu einem Projektboard eingeladen");
      message.setText(buildAccountInvitedEmailBody(toEmail, projectBoard));
      message.setFrom("noreply@tmws.local");
      mailSender.send(message);
    } catch (MailSendException e) {
      logger.log(Level.WARNING, "E-Mail konnte nicht versandt werden. Server offline?", e);
    }
  }

  /**
   * Sendet eine E-Mail, die den Benutzer darüber informiert, dass sein Account zu einer Aufgabe
   * zugewiesen wurde.
   *
   * @param toEmail Die E-Mail-Adresse des Empfängers.
   * @param projectBoard Der Name des Projektboards, zu dem die Aufgabe gehört, damit der Benutzer
   *     überprüfen kann, ob es sich um ein Projektboard handelt, zu dem er Zugang haben sollte.
   * @param statusCategory Die Statuskategorie, zu der die Aufgabe gehört, damit der Benutzer die
   *     Aufgabe leichter finden kann.
   * @param task Der Name der Aufgabe, zu der der Benutzer zugewiesen wurde, damit er die Aufgabe
   *     leichter finden kann.
   */
  @Async
  public void sendAccountAssignedEmail(
      String toEmail, String projectBoard, String statusCategory, String task) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Account zu einer Aufgabe zugewiesen");
      message.setText(buildAccountAssignedEmailBody(toEmail, projectBoard, statusCategory, task));
      message.setFrom("noreply@tmws.local");
      mailSender.send(message);
    } catch (MailSendException e) {
      logger.log(Level.WARNING, "E-Mail konnte nicht versandt werden. Server offline?", e);
    }
  }

  /**
   * Sendet eine E-Mail, die den Benutzer darüber informiert, dass sein Account von einer Aufgabe
   * entfernt wurde.
   *
   * @param toEmail Die E-Mail-Adresse des Empfängers.
   * @param projectBoard Der Name des Projektboards, zu dem die Aufgabe gehört, damit der Benutzer
   *     überprüfen kann, ob es sich um ein Projektboard handelt, zu dem er Zugang haben sollte.
   * @param statusCategory Die Statuskategorie, zu der die Aufgabe gehört, damit der Benutzer die
   *     Aufgabe leichter finden kann.
   * @param task Der Name der Aufgabe, von der der Benutzer entfernt wurde, damit er die Aufgabe
   *     leichter finden kann.
   */
  @Async
  public void sendAccountUnassignedEmail(
      String toEmail, String projectBoard, String statusCategory, String task) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Von Aufgabe entfernt");
      message.setText(buildAccountUnassignedEmailBody(toEmail, projectBoard, statusCategory, task));
      message.setFrom("noreply@tmws.local");
      mailSender.send(message);
    } catch (MailSendException e) {
      logger.log(Level.WARNING, "E-Mail konnte nicht versandt werden. Server offline?", e);
    }
  }

  /**
   * Sendet eine E-Mail, die den Benutzer darüber informiert, dass sein Account zum Besitzer eines
   * Projektboards ernannt wurde.
   *
   * @param toEmail Die E-Mail-Adresse des Empfängers.
   * @param projectBoard Der Name des Projektboards, von dem der Benutzer zum Besitzer ernannt
   *     wurde, damit der Benutzer überprüfen kann, ob es sich um ein Projektboard handelt, zu dem
   *     er Zugang haben sollte.
   */
  @Async
  public void sendNewProjectboardOwnerEmail(String toEmail, String projectBoard) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Besitzer eines Projektboards ernannt");
      message.setText(buildNewProjectboardOwnerEmailBody(toEmail, projectBoard));
      message.setFrom("noreply@tmws.local");
      mailSender.send(message);
    } catch (MailSendException e) {
      logger.log(Level.WARNING, "E-Mail konnte nicht versandt werden. Server offline?", e);
    }
  }

  /**
   * Hilfsmethode zum Erstellen des E-Mail-Textes für die Registrierungsbestätigung, der den
   * Verifizierungslink enthält.
   *
   * @param verificationLink Der Link, den der Benutzer anklicken muss, um seine Registrierung
   *     abzuschließen.
   * @return Der E-Mail-Text für die Registrierungsbestätigung.
   */
  private String buildRegistrationEmailBody(String verificationLink) {
    return "Hallo,\n\n"
        + "vielen Dank für die Registrierung bei dem Task Management Web System (TMWS)!\n\n"
        + "Bitte klicken Sie auf den folgenden Link, um Ihre Registrierung abzuschließen und"
        + " sich anzumelden:\n\n"
        + verificationLink
        + "\n\n"
        + "Dieser Link ist 24 Stunden gültig.\n\n"
        + "Falls Sie sich nicht registriert haben, ignorieren Sie diese E-Mail.\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

  /**
   * Hilfsmethode zum Erstellen des E-Mail-Textes für die Passwort-Zurücksetzen-E-Mail, der den Link
   * zum Erstellen eines neuen Passworts enthält.
   *
   * @param resetLink Der Link, den der Benutzer anklicken muss, um ein neues Passwort zu erstellen.
   * @return Der E-Mail-Text für die Passwort-Zurücksetzen-E-Mail.
   */
  private String buildPasswordResetEmailBody(String resetLink) {
    return "Hallo,\n\n"
        + "Sie haben eine Anfrage zum Zurücksetzen Ihres Passworts gestellt.\n\n"
        + "Bitte klicken Sie auf den folgenden Link, um ein neues Passwort zu erstellen:\n\n"
        + resetLink
        + "\n\n"
        + "Dieser Link ist 1 Stunde gültig.\n\n"
        + "Falls Sie diesen Link nicht angefordert haben, ignorieren Sie diese E-Mail.\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

  /**
   * Hilfsmethode zum Erstellen des E-Mail-Textes für die Passwort-Änderungs-E-Mail, der die
   * E-Mail-Adresse des Accounts enthält, dessen Passwort geändert wurde.
   *
   * @param email Die E-Mail-Adresse des Accounts, dessen Passwort geändert wurde, damit der
   *     Benutzer überprüfen kann, ob es sich um seinen Account handelt.
   * @return Der E-Mail-Text für die Passwort-Änderungs-E-Mail.
   */
  private String buildPasswordChangedEmailBody(String email) {
    return "Hallo,\n\n"
        + "Ihr Passwort wurde erfolgreich für folgenden Account geändert:\n\n"
        + email
        + "\n\n"
        + "Falls Sie dies nicht waren, ist Ihr Account womöglich kompromittiert.\n\n"
        + "Kontaktieren Sie uns so schnell wie möglich, damit wir den Account deaktivieren"
        + " können.\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

  /**
   * Hilfsmethode zum Erstellen des E-Mail-Textes für die Account-Deaktivierungs-E-Mail, der die
   * E-Mail-Adresse des Accounts enthält, der deaktiviert wurde.
   *
   * @param email Die E-Mail-Adresse des Accounts, der deaktiviert wurde, damit der Benutzer
   *     überprüfen kann, ob es sich um seinen Account handelt.
   * @return Der E-Mail-Text für die Account-Deaktivierungs-E-Mail.
   */
  private String buildAccountDeactivationEmailBody(String email) {
    return "Hallo,\n\n"
        + "Ihr folgender Account wurde erfolgreich deaktiviert:\n\n"
        + email
        + "\n\n"
        + "Kontaktieren Sie uns, falls Sie eine vollständige Löschung wollen.\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

  /**
   * Hilfsmethode zum Erstellen des E-Mail-Textes für die Account-Invited-E-Mail, der die
   * E-Mail-Adresse des Accounts und den Namen des Projektboards enthält, zu dem der Benutzer
   * eingeladen wurde.
   *
   * @param email Die E-Mail-Adresse des Accounts, der eingeladen wurde, damit der Benutzer
   *     überprüfen kann, ob es sich um seinen Account handelt.
   * @param projectBoard Der Name des Projektboards, zu dem der Benutzer eingeladen wurde, damit er
   *     überprüfen kann, ob es sich um ein Projektboard handelt, zu dem er Zugang haben sollte.
   * @return Der E-Mail-Text für die Account-Invited-E-Mail.
   */
  private String buildAccountInvitedEmailBody(String email, String projectBoard) {
    return "Hallo,\n\n"
        + "Ihr Account "
        + email
        + " wurde zum folgenden Projektboard eingeladen:\n\n"
        + projectBoard
        + "\n\n"
        + "Sie können diesem Projektboard beitreten, indem Sie sich bei TMWS anmelden und das"
        + " Projektboard in Ihrer Übersicht auswählen.\n\n"
        + "Kontaktieren Sie uns, falls Sie denken, dass hier ein Fehler vorliegt.\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

  /**
   * Hilfsmethode zum Erstellen des E-Mail-Textes für die Account-Assigned-E-Mail, der die
   * E-Mail-Adresse des Accounts, den Namen des Projektboards, die Statuskategorie und den Namen der
   * Aufgabe enthält, zu der der Benutzer zugewiesen wurde.
   *
   * @param email Die E-Mail-Adresse des Accounts, der zu einer Aufgabe zugewiesen wurde, damit der
   *     Benutzer überprüfen kann, ob es sich um seinen Account handelt.
   * @param projectBoard Der Name des Projektboards, zu dem die Aufgabe gehört, damit der Benutzer
   *     überprüfen kann, ob es sich um ein Projektboard handelt, zu dem er Zugang haben sollte.
   * @param statusCategory Die Statuskategorie, zu der die Aufgabe gehört, damit der Benutzer die
   *     Aufgabe leichter finden kann.
   * @param task Der Name der Aufgabe, zu der der Benutzer zugewiesen wurde, damit er die Aufgabe
   *     leichter finden kann.
   * @return Der E-Mail-Text für die Account-Assigned-E-Mail.
   */
  private String buildAccountAssignedEmailBody(
      String email, String projectBoard, String statusCategory, String task) {
    return "Hallo,\n\n"
        + "Ihr Account "
        + email
        + " wurde zur folgenden Aufgabe zugewiesen:\n\n"
        + task
        + "\n\n"
        + "Diese Aufgabe ist unter der Statuskategorie "
        + statusCategory
        + " bei dem Projektboard "
        + projectBoard
        + " zu finden.\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

  /**
   * Hilfsmethode zum Erstellen des E-Mail-Textes für die Account-Unassigned-E-Mail, der die
   * E-Mail-Adresse des Accounts, den Namen des Projektboards, die Statuskategorie und den Namen der
   * Aufgabe enthält, von der der Benutzer entfernt wurde.
   *
   * @param email Die E-Mail-Adresse des Accounts, der von einer Aufgabe entfernt wurde, damit der
   *     Benutzer überprüfen kann, ob es sich um seinen Account handelt.
   * @param projectBoard Der Name des Projektboards, zu dem die Aufgabe gehört, damit der Benutzer
   *     überprüfen kann, ob es sich um ein Projektboard handelt, zu dem er Zugang haben sollte.
   * @param statusCategory Die Statuskategorie, zu der die Aufgabe gehört, damit der Benutzer die
   *     Aufgabe leichter finden kann.
   * @param task Der Name der Aufgabe, von der der Benutzer entfernt wurde, damit er die Aufgabe
   *     leichter finden kann.
   * @return Der E-Mail-Text für die Account-Unassigned-E-Mail.
   */
  private String buildAccountUnassignedEmailBody(
      String email, String projectBoard, String statusCategory, String task) {
    return "Hallo,\n\n"
        + "Ihr Account "
        + email
        + " wurde von der folgenden Aufgabe entfernt:\n\n"
        + task
        + "\n\n"
        + "Diese Aufgabe befindet sich unter der Statuskategorie "
        + statusCategory
        + " im Projektboard "
        + projectBoard
        + ".\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

  /**
   * Hilfsmethode zum Erstellen des E-Mail-Textes für die New-Projectboard-Owner-E-Mail, der die
   * E-Mail-Adresse des Accounts und den Namen des Projektboards enthält, von dem der Benutzer zum
   * Besitzer ernannt wurde.
   *
   * @param email Die E-Mail-Adresse des Accounts, der zum Besitzer eines Projektboards ernannt
   *     wurde, damit der Benutzer überprüfen kann, ob es sich um seinen Account handelt.
   * @param projectBoard Der Name des Projektboards, von dem der Benutzer zum Besitzer ernannt
   *     wurde, damit der Benutzer überprüfen kann, ob es sich um ein Projektboard handelt, zu dem
   *     er Zugang haben sollte.
   * @return Der E-Mail-Text für die New-Projectboard-Owner-E-Mail.
   */
  private String buildNewProjectboardOwnerEmailBody(String email, String projectBoard) {
    return "Hallo,\n\n"
        + "Ihr Account "
        + email
        + " wurde zum Besitzer vom folgenden Projektboard ernannt:\n\n"
        + projectBoard
        + "\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }
}
