/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein ungültiges
 * Token erkannt wird.
 */
package io.github.mexikoedi.tmws.security.exception;

public class InvalidTokenException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public InvalidTokenException(String message) {
    super(message);
  }
}
