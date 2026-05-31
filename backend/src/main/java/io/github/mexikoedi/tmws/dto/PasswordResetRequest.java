/**
 * Diese Klasse repräsentiert die Anforderung zum Zurücksetzen des Passworts.
 */
package io.github.mexikoedi.tmws.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordResetRequest {
  @NotBlank(message = "Neues Passwort ist erforderlich.")
  @Size(min = 8, max = 50, message = "Passwort muss zwischen 8 und 50 Zeichen lang sein.")
  @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$", message = "Passwort muss Groß-/Kleinbuchstaben, Zahl und Sonderzeichen enthalten.")
  private String password;

  /**
   * Konstruktor für die PasswordResetRequest-Klasse.
   * Für Tests benutzt.
   *
   * @param password Das neue Passwort, das der Benutzer festlegen möchte.
   */
  public PasswordResetRequest(String password) {
    this.password = password;
  }
}
