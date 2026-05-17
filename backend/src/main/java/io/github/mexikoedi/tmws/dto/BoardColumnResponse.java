package io.github.mexikoedi.tmws.dto;

import java.util.List;

public class BoardColumnResponse {
  private long id;
  private String title;
  private int position;
  private long boardId;
  private List<TaskResponse> tasks;

  // Getter / Setter
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public long getBoardId() {
    return boardId;
  }

  public void setBoardId(long boardId) {
    this.boardId = boardId;
  }

  public List<TaskResponse> getTasks() {
    return tasks;
  }

  public void setTasks(List<TaskResponse> tasks) {
    this.tasks = tasks;
  }
}
