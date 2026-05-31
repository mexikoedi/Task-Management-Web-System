/** Diese Klasse repräsentiert die Daten, die für eine Login-Anfrage benötigt werden. */
package io.github.mexikoedi.tmws.dto;

import io.github.mexikoedi.tmws.util.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
public class LoginRequest {
  @NotBlank(message = "E-Mail ist erforderlich.")
  @Size(max = 30, message = "E-Mail darf nicht länger als 30 Zeichen lang sein.")
  @ValidEmail
  private String email;

  @NotBlank(message = "Passwort ist erforderlich.")
  private String password;

  /**
   * Konstruktor für die LoginRequest-Klasse. Für Tests benutzt.
   *
   * @param email E-Mail-Adresse des Benutzers.
   * @param password Passwort des Benutzers.
   */
  public LoginRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }
}
