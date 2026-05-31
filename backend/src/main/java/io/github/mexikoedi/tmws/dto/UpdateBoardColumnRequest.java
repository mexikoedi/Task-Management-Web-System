/**
 * Diese Klasse repräsentiert die Anforderung zum Aktualisieren einer Spalte in einem Board.
 */
package io.github.mexikoedi.tmws.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateBoardColumnRequest {
  @NotBlank(message = "Titel ist erforderlich.")
  @Size(max = 25, message = "Titel darf nicht länger als 25 Zeichen lang sein.")
  private String title;

  /**
   * Konstruktor für die UpdateBoardColumnRequest-Klasse.
   * Für Tests benutzt.
   *
   * @param title Der neue Titel der Spalte.
   */
  public UpdateBoardColumnRequest(String title) {
    this.title = title;
  }
}
