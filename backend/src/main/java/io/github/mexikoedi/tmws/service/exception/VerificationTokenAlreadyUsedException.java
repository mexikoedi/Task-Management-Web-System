/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn ein
 * Verifizierungs-Token bereits verwendet wurde.
 */
package io.github.mexikoedi.tmws.service.exception;

public class VerificationTokenAlreadyUsedException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public VerificationTokenAlreadyUsedException(String message) {
    super(message);
  }
}
