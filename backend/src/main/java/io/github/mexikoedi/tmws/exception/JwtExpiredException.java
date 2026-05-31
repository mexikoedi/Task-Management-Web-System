/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein JWT-Token
 * abgelaufen ist und nicht mehr verwendet werden kann.
 */
package io.github.mexikoedi.tmws.exception;

public class JwtExpiredException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public JwtExpiredException(String message) {
    super(message);
  }
}
