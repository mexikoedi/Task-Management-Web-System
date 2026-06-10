/**
 * Diese Klasse repräsentiert die Daten, die für die Registrierung eines neuen Benutzers
 * erforderlich sind.
 */
package io.github.mexikoedi.tmws.service.dto;

import io.github.mexikoedi.tmws.service.validation.PasswordConfirmation;
import io.github.mexikoedi.tmws.service.validation.PasswordMatches;
import io.github.mexikoedi.tmws.service.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@PasswordMatches
public class RegisterRequest implements PasswordConfirmation {
  @NotBlank(message = "Name ist erforderlich.")
  @Size(min = 2, max = 30, message = "Name muss zwischen 2 und 30 Zeichen lang sein.")
  private String name;

  @NotBlank(message = "E-Mail ist erforderlich.")
  @Size(max = 30, message = "E-Mail darf nicht länger als 30 Zeichen lang sein.")
  @ValidEmail
  private String email;

  @NotBlank(message = "Passwort ist erforderlich.")
  @Size(min = 8, max = 50, message = "Passwort muss zwischen 8 und 50 Zeichen lang sein.")
  @Pattern(
      regexp =
          "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
      message = "Passwort muss Groß-/Kleinbuchstaben, Zahl und Sonderzeichen enthalten.")
  private String password;

  @NotBlank(message = "Passwortbestätigung ist erforderlich.")
  private String passwordConfirm;

  /**
   * Konstruktor für die Registrierung eines neuen Benutzers. Für Tests benutzt.
   *
   * @param name Name des Benutzers.
   * @param email E-Mail des Benutzers.
   * @param password Passwort des Benutzers.
   */
  public RegisterRequest(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
  }

  /**
   * Gibt das Passwort zurück, das für die Registrierung verwendet wird. Für Validierung benutzt.
   *
   * @return Das Passwort des Benutzers.
   */
  @Override
  public String getPassword() {
    return password;
  }

  /**
   * Gibt die Passwortbestätigung zurück, die für die Registrierung verwendet wird. Für Validierung
   * benutzt.
   *
   * @return Die Passwortbestätigung des Benutzers.
   */
  @Override
  public String getPasswordConfirm() {
    return passwordConfirm;
  }

  /**
   * Gibt den Namen des Passwortbestätigungsfeldes zurück, der für die Validierung verwendet wird.
   * Für Validierung benutzt.
   *
   * @return Der Name des Passwortbestätigungsfeldes.
   */
  @Override
  public String getPasswordConfirmFieldName() {
    return "passwordConfirm";
  }
}
