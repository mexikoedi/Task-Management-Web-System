/** Diese Klasse repräsentiert die Anforderung zum Einladen eines Benutzers zu einem Board. */
package io.github.mexikoedi.tmws.service.dto;

import io.github.mexikoedi.tmws.util.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InviteBoardRequest {
  @NotBlank(message = "E-Mail ist erforderlich.")
  @Size(max = 30, message = "E-Mail darf nicht länger als 30 Zeichen lang sein.")
  @ValidEmail
  private String email;

  /**
   * Konstruktor für die InviteBoardRequest-Klasse. Für Tests benutzt.
   *
   * @param email E-Mail-Adresse des Benutzers, der eingeladen werden soll.
   */
  public InviteBoardRequest(String email) {
    this.email = email;
  }
}
