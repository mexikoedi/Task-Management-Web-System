/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein
 * Passwort-Reset-Token abgelaufen ist.
 */
package io.github.mexikoedi.tmws.exception;

public class PasswordResetTokenExpiredException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public PasswordResetTokenExpiredException(String message) {
    super(message);
  }
}
