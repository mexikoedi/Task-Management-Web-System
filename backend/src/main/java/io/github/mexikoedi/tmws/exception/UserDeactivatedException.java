/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein
 * Benutzerkonto deaktiviert ist.
 */
package io.github.mexikoedi.tmws.exception;

public class UserDeactivatedException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public UserDeactivatedException(String message) {
    super(message);
  }
}
