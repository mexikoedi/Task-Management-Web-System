/**
 * Diese Klasse ist eine Hilfsklasse zur Generierung von Tokens.
 */
package io.github.mexikoedi.tmws.security;

import java.util.UUID;

public class TokenGenerator {
  /**
   * Generiert ein eindeutiges Token, das als String zurückgegeben wird.
   * Genutzt für VerificationToken/PasswordResetToken.
   *
   * @return Ein eindeutiges Token als String.
   */
  public static String generateToken() {
    return UUID.randomUUID().toString();
  }
}
