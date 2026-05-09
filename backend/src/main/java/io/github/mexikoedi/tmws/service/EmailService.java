package io.github.mexikoedi.tmws.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

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

    private String buildRegistrationEmailBody(String verificationLink) {
        return "Hallo,\n\n" +
                "vielen Dank für die Registrierung bei dem Task Management Web System (TMWS)!\n\n" +
                "Bitte klicken Sie auf den folgenden Link, um Ihre Registrierung abzuschließen und sich anzumelden:\n\n" +
                verificationLink + "\n\n" +
                "Dieser Link ist 24 Stunden gültig.\n\n" +
                "Falls Sie sich nicht registriert haben, ignorieren Sie diese E-Mail.\n\n" +
                "Mit freundlichen Grüßen,\n" +
                "Das TMWS-Team";
    }

    private String buildPasswordResetEmailBody(String resetLink) {
        return "Hallo,\n\n" +
                "Sie haben eine Anfrage zum Zurücksetzen Ihres Passworts gestellt.\n\n" +
                "Bitte klicken Sie auf den folgenden Link, um ein neues Passwort zu erstellen:\n\n" +
                resetLink + "\n\n" +
                "Dieser Link ist 1 Stunde gültig.\n\n" +
                "Falls Sie diesen Link nicht angefordert haben, ignorieren Sie diese E-Mail.\n\n" +
                "Mit freundlichen Grüßen,\n" +
                "Das TMWS-Team";
    }
}



