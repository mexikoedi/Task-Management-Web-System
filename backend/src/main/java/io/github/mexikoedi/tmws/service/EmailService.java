package io.github.mexikoedi.tmws.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Async
  public void sendRegistrationEmail(String toEmail, String verificationLink) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Registrierungsbestätigung");
      message.setText(buildRegistrationEmailBody(verificationLink));
      message.setFrom("noreply@tmws.local");

      mailSender.send(message);
      System.out.println("Registration email sent to: " + toEmail);
    } catch (Exception e) {
      System.err.println("Failed to send registration email to: " + toEmail);
      e.printStackTrace();
      System.out.println("TMWS development mode: continuing without SMTP delivery.");
    }
  }

  @Async
  public void sendPasswordResetEmail(String toEmail, String resetLink) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Passwort zurücksetzen");
      message.setText(buildPasswordResetEmailBody(resetLink));
      message.setFrom("noreply@tmws.local");

      mailSender.send(message);
      System.out.println("Password reset email sent to: " + toEmail);
    } catch (Exception e) {
      System.err.println("Failed to send password reset email to: " + toEmail);
      e.printStackTrace();
      System.out.println("TMWS development mode: continuing without SMTP delivery.");
    }
  }

  @Async
  public void sendPasswordChangedEmail(String toEmail, String email) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Passwort geändert");
      message.setText(buildPasswordChangedEmailBody(email));
      message.setFrom("noreply@tmws.local");

      mailSender.send(message);
      System.out.println("Password changed email sent to: " + toEmail);
    } catch (Exception e) {
      System.err.println("Failed to send password changed email to: " + toEmail);
      e.printStackTrace();
      System.out.println("TMWS development mode: continuing without SMTP delivery.");
    }
  }

  @Async
  public void sendAccountDeactivationEmail(String toEmail, String email) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Account deaktiviert");
      message.setText(buildAccountDeactivationEmailBody(email));
      message.setFrom("noreply@tmws.local");

      mailSender.send(message);
      System.out.println("Account deactivated email sent to: " + toEmail);
    } catch (Exception e) {
      System.err.println("Failed to send account deactivated email to: " + toEmail);
      e.printStackTrace();
      System.out.println("TMWS development mode: continuing without SMTP delivery.");
    }
  }

  @Async
  public void sendAccountInvitedEmail(String toEmail, String projectBoard) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Account zu einem Projektboard eingeladen");
      message.setText(buildAccountInvitedEmailBody(toEmail, projectBoard));
      message.setFrom("noreply@tmws.local");

      mailSender.send(message);
      System.out.println("Account invited email sent to: " + toEmail);
    } catch (Exception e) {
      System.err.println("Failed to send account invited email to: " + toEmail);
      e.printStackTrace();
      System.out.println("TMWS development mode: continuing without SMTP delivery.");
    }
  }

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
      System.out.println("Account assigned email sent to: " + toEmail);
    } catch (Exception e) {
      System.err.println("Failed to send account assigned email to: " + toEmail);
      e.printStackTrace();
      System.out.println("TMWS development mode: continuing without SMTP delivery.");
    }
  }

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
      System.out.println("Account unassigned email sent to: " + toEmail);
    } catch (Exception e) {
      System.err.println("Failed to send unassigned email to: " + toEmail);
      e.printStackTrace();
      System.out.println("TMWS development mode: continuing without SMTP delivery.");
    }
  }

  @Async
  public void sendNewProjectboardOwnerEmail(String toEmail, String projectBoard) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(toEmail);
      message.setSubject("TMWS - Besitzer eines Projektboards ernannt");
      message.setText(buildNewProjectboardOwnerEmailBody(toEmail, projectBoard));
      message.setFrom("noreply@tmws.local");

      mailSender.send(message);
      System.out.println("New board owner email sent to: " + toEmail);
    } catch (Exception e) {
      System.err.println("Failed to send new board owner email to: " + toEmail);
      e.printStackTrace();
      System.out.println("TMWS development mode: continuing without SMTP delivery.");
    }
  }

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

  private String buildAccountDeactivationEmailBody(String email) {
    return "Hallo,\n\n"
        + "Ihr folgender Account wurde erfolgreich deaktiviert:\n\n"
        + email
        + "\n\n"
        + "Kontaktieren Sie uns, falls Sie eine vollständige Löschung wollen.\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

  private String buildAccountInvitedEmailBody(String email, String projectBoard) {
    return "Hallo,\n\n"
        + "Ihr Account "
        + email
        + " wurde zum folgenden Projektboard eingeladen:\n\n"
        + projectBoard
        + "\n\n"
        + "Sie können diesem Projektboard beitreten, indem Sie sich bei TMWS anmelden und das Projektboard in Ihrer Übersicht auswählen.\n\n"
        + "Kontaktieren Sie uns, falls Sie denken, dass hier ein Fehler vorliegt.\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

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
        + " bei dem Projekboard "
        + projectBoard
        + " zu finden.\n\n"
        + "Mit freundlichen Grüßen,\n"
        + "Das TMWS-Team";
  }

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
