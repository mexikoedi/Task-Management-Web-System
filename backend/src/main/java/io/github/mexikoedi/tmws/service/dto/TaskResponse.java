/**
 * Diese Klasse repräsentiert die Antwort, die zurückgegeben wird, wenn eine Anfrage für eine
 * Aufgabe gestellt wird.
 */
package io.github.mexikoedi.tmws.service.dto;

import io.github.mexikoedi.tmws.model.Task;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskResponse {
  private Long id;
  private String title;
  private String description;
  private String deadline;
  private Integer position;
  private String labels;
  private String attachments;
  private List<UserResponse> assignees;

  public static TaskResponse fromEntity(Task task) {
    TaskResponse r = new TaskResponse();
    r.id = task.getId();
    r.title = task.getTitle();
    r.description = task.getDescription();
    r.position = task.getPosition();
    r.labels = task.getLabels();
    r.attachments = task.getAttachments();
    r.deadline = task.getDeadline() != null ? task.getDeadline().toString() : null;
    r.assignees = task.getAssignees().stream().map(UserResponse::new).collect(Collectors.toList());

    return r;
  }
}
