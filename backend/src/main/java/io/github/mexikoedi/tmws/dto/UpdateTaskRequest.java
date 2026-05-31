/**
 * Diese Klasse repräsentiert die Daten, die für die Aktualisierung einer Aufgabe erforderlich sind.
 */
package io.github.mexikoedi.tmws.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class UpdateTaskRequest {
  @NotBlank(message = "Titel ist erforderlich.")
  @Size(max = 30, message = "Titel darf nicht länger als 30 Zeichen lang sein.")
  private String title;

  @Size(max = 1000, message = "Beschreibung darf nicht länger als 1000 Zeichen lang sein.")
  private String description;

  @FutureOrPresent(message = "Deadline darf nicht in der Vergangenheit liegen.")
  private LocalDate deadline;

  @Size(max = 255, message = "Labels dürfen nicht länger als 255 Zeichen lang sein.")
  private String labels;

  @Size(max = 255, message = "Anhänge dürfen nicht länger als 255 Zeichen lang sein.")
  private String attachments;

  private List<Long> assigneeIds;

  /**
   * Konstruktor, der die Felder basierend auf den übergebenen Objekten initialisiert.
   * Es wird versucht, die Objekte in die entsprechenden Typen zu konvertieren, falls sie nicht null sind.
   * Für Tests benutzt.
   *
   * @param title Der Titel der Aufgabe.
   * @param description Die Beschreibung der Aufgabe.
   * @param deadline Das Fälligkeitsdatum der Aufgabe.
   * @param labels Die Labels der Aufgabe.
   * @param attachments Die Anhänge der Aufgabe.
   * @param assigneeIds Die IDs der zugewiesenen Benutzer für die Aufgabe.
   */
  public UpdateTaskRequest(String title, Object description, Object deadline, Object labels, Object attachments, Object assigneeIds) {
    this.title = title;
    this.description = description != null ? description.toString() : null;
    this.deadline = deadline != null ? LocalDate.parse(deadline.toString()) : null;
    this.labels = labels != null ? labels.toString() : null;
    this.attachments = attachments != null ? attachments.toString() : null;

    if (assigneeIds instanceof List) {
      this.assigneeIds = ((List<?>) assigneeIds).stream().filter(id -> id instanceof Number).map(id -> ((Number) id).longValue()).toList();
    } else {
      this.assigneeIds = null;
    }
  }
}
