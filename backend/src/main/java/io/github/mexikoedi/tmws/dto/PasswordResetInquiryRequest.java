/**
 * Diese Klasse repräsentiert die Daten, die für eine Anfrage zum Zurücksetzen des Passworts
 * erforderlich sind.
 */
package io.github.mexikoedi.tmws.dto;

import io.github.mexikoedi.tmws.util.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
public class PasswordResetInquiryRequest {
  @NotBlank(message = "E-Mail ist erforderlich.")
  @Size(max = 30, message = "E-Mail darf nicht länger als 30 Zeichen lang sein.")
  @ValidEmail
  private String email;

  /**
   * Konstruktor für die PasswordResetInquiryRequest-Klasse. Für Tests benutzt.
   *
   * @param email Die E-Mail-Adresse des Benutzers, der das Passwort zurücksetzen möchte.
   */
  public PasswordResetInquiryRequest(String email) {
    this.email = email;
  }
}
