package io.github.mexikoedi.tmws.util;

import java.util.regex.Pattern;

public class PasswordValidator {
    /**
     * Validiert ein Passwort nach folgenden Kriterien:
     * - Mindestens 8 Zeichen
     * - Mindestens ein Großbuchstabe
     * - Mindestens ein Kleinbuchstabe
     * - Mindestens eine Ziffer
     * - Mindestens ein Sonderzeichen (!@#$%^&*)
     */
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValid(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    public static String getPasswordRequirements() {
        return "Passwort muss mindestens 8 Zeichen, einen Großbuchstaben, einen Kleinbuchstaben, eine Ziffer und ein Sonderzeichen (!@#$%^&*) enthalten";
    }
}

