package io.github.mexikoedi.tmws.dto;

import io.github.mexikoedi.tmws.model.Task;
import java.util.List;
import java.util.stream.Collectors;

public class TaskResponse {
  private Long id;
  private String title;
  private String description;
  private String deadline;
  private Integer position;
  private String labels;
  private String attachments;
  private List<UserSummaryResponse> assignees;

  public static TaskResponse fromEntity(Task task) {
    TaskResponse r = new TaskResponse();
    r.id = task.getId();
    r.title = task.getTitle();
    r.description = task.getDescription();
    r.position = task.getPosition();
    r.labels = task.getLabels();
    r.attachments = task.getAttachments();
    r.deadline = task.getDeadline() != null ? task.getDeadline().toString() : null;

    r.assignees =
        task.getAssignees().stream()
            .map(
                u ->
                    new UserSummaryResponse(
                        u.getId(), u.getName(), u.getEmail(), u.isEmailChanged(), u.getImage()))
            .collect(Collectors.toList());

    return r;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDeadline() {
    return deadline;
  }

  public void setDeadline(String deadline) {
    this.deadline = deadline;
  }

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  public String getLabels() {
    return labels;
  }

  public void setLabels(String labels) {
    this.labels = labels;
  }

  public String getAttachments() {
    return attachments;
  }

  public void setAttachments(String attachments) {
    this.attachments = attachments;
  }

  public List<UserSummaryResponse> getAssignees() {
    return assignees;
  }

  public void setAssignees(List<UserSummaryResponse> assignees) {
    this.assignees = assignees;
  }
}
