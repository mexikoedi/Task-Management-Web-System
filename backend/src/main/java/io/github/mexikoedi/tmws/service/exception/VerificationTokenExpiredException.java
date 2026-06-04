/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein
 * Verifizierungs-Token abgelaufen ist.
 */
package io.github.mexikoedi.tmws.service.exception;

public class VerificationTokenExpiredException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public VerificationTokenExpiredException(String message) {
    super(message);
  }
}
