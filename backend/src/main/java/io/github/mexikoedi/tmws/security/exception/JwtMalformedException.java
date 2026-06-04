/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein JWT als
 * fehlerhaft erkannt wird.
 */
package io.github.mexikoedi.tmws.security.exception;

public class JwtMalformedException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public JwtMalformedException(String message) {
    super(message);
  }
}
