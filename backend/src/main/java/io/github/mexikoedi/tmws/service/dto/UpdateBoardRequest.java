/**
 * Diese Klasse repräsentiert die Daten, die für die Aktualisierung eines Boards erforderlich sind.
 */
package io.github.mexikoedi.tmws.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateBoardRequest {
  @NotBlank(message = "Titel ist erforderlich.")
  @Size(max = 30, message = "Titel darf nicht länger als 30 Zeichen lang sein.")
  private String title;

  @Size(max = 255, message = "Hintergrundbild darf nicht länger als 255 Zeichen lang sein.")
  @Pattern(
      regexp = "^(|https?://.+\\.(png|jpg|jpeg|webp)(\\?.*)?)$",
      message = "Nur gültige Bild-URLs erlaubt.")
  private String background;
}
