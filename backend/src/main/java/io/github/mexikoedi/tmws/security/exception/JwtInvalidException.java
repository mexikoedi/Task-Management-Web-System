/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein ungültiges
 * JWT erkannt wird.
 */
package io.github.mexikoedi.tmws.security.exception;

public class JwtInvalidException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public JwtInvalidException(String message) {
    super(message);
  }
}
