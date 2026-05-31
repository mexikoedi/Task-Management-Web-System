/**
 * Diese KLasse reprsentiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn versucht
 * wird, eine Spalte hinzuzufgen, die bereits existiert.
 */
package io.github.mexikoedi.tmws.exception;

public class ColumnAlreadyExistsException extends RuntimeException {
  /**
   * Konstruktor, der eine benutzerdefinierte Fehlermeldung akzeptiert.
   *
   * @param message Die Fehlermeldung, die die Details der Ausnahme beschreibt.
   */
  public ColumnAlreadyExistsException(String message) {
    super(message);
  }
}
