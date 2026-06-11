/**
 * Diese KLasse repräsentiert eine benutzerdefinierte Ausnahme, die ausgelöst wird, wenn versucht
 * wird, eine Spalte hinzuzufügen, die bereits existiert.
 */
package io.github.mexikoedi.tmws.service.exception;

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
