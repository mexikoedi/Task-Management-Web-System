/**
 * Dieses Interface definiert die Methoden, die von Klassen implementiert werden müssen, die eine
 * Passwortbestätigung benötigen.
 */
package io.github.mexikoedi.tmws.service.validation;

public interface PasswordConfirmation {
  /**
   * Gibt das Passwort zurück, das bestätigt werden soll.
   *
   * @return Das Passwort, das bestätigt werden soll.
   */
  String getPassword();

  /**
   * Gibt das Passwort zurück, das zur Bestätigung verwendet wird.
   *
   * @return Das Passwort, das zur Bestätigung verwendet wird.
   */
  String getPasswordConfirm();

  /**
   * Gibt den Namen des Passwortbestätigungsfeldes zurück, der in Fehlermeldungen verwendet wird.
   *
   * @return Der Name des Passwortbestätigungsfeldes, der in Fehlermeldungen verwendet wird.
   */
  String getPasswordConfirmFieldName();
}
