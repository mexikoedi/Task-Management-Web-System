/**
 * Diese Klasse definiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn versucht wird, eine Aufgabe
 * zu erstellen, die bereits existiert.
 */
package io.github.mexikoedi.tmws.exception;

public class TaskAlreadyExistsException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public TaskAlreadyExistsException(String message) {
    super(message);
  }
}
