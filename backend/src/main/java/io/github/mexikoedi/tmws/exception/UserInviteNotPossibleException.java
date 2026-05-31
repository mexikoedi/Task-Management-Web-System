/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn eine Einladung
 * eines Benutzers nicht möglich ist.
 */
package io.github.mexikoedi.tmws.exception;

public class UserInviteNotPossibleException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public UserInviteNotPossibleException(String message) {
    super(message);
  }
}
