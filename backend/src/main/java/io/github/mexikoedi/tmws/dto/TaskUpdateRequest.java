package io.github.mexikoedi.tmws.dto;

import java.util.List;

public class TaskUpdateRequest {
  public String title;
  public String description;
  public String deadline;
  public String labels;
  public String attachments;
  public List<Long> assigneeIds;
}
