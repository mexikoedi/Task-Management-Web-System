/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein Benutzer
 * bereits Mitglied eines Teams ist und versucht, erneut eingeladen zu werden.
 */
package io.github.mexikoedi.tmws.exception;

public class UserAlreadyMemberException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public UserAlreadyMemberException(String message) {
    super(message);
  }
}
