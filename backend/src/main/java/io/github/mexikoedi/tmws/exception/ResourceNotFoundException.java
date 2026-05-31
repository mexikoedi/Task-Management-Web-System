/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn eine angeforderte Ressource
 * nicht gefunden wird.
 */
package io.github.mexikoedi.tmws.exception;

public class ResourceNotFoundException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
