/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn versucht wird,
 * eine E-Mail-Adresse zu registrieren, die bereits in der Datenbank vorhanden ist.
 */
package io.github.mexikoedi.tmws.exception;

public class EmailAlreadyExistsException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public EmailAlreadyExistsException(String message) {
    super(message);
  }
}
