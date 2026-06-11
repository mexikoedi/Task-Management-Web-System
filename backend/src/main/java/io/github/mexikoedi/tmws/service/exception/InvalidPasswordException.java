/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein ungültiges
 * Passwort eingegeben wird.
 */
package io.github.mexikoedi.tmws.service.exception;

public class InvalidPasswordException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public InvalidPasswordException(String message) {
    super(message);
  }
}
