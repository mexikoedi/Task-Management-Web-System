/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein
 * Passwort-Reset-Token bereits verwendet wurde.
 */
package io.github.mexikoedi.tmws.exception;

public class PasswordResetTokenAlreadyUsedException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public PasswordResetTokenAlreadyUsedException(String message) {
    super(message);
  }
}
