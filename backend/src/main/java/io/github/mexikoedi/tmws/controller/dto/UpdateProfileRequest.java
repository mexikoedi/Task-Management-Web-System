/**
 * Diese Klasse repräsentiert die Daten, die für die Aktualisierung eines Benutzerprofils
 * erforderlich sind.
 */
package io.github.mexikoedi.tmws.controller.dto;

import io.github.mexikoedi.tmws.service.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateProfileRequest {
  @NotBlank(message = "Name ist erforderlich.")
  @Size(min = 2, max = 30, message = "Name muss zwischen 2 und 30 Zeichen lang sein.")
  private String name;

  @NotBlank(message = "E-Mail ist erforderlich.")
  @Size(max = 30, message = "E-Mail darf nicht länger als 30 Zeichen lang sein.")
  @ValidEmail
  private String newEmail;

  @Size(max = 255, message = "Profilbild darf nicht länger als 255 Zeichen lang sein.")
  @Pattern(
      regexp = "^(|https?://.+\\.(png|jpg|jpeg|webp)(\\?.*)?)$",
      message = "Nur gültige Bild-URLs erlaubt.")
  private String image;
}
