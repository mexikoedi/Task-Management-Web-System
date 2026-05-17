package io.github.mexikoedi.tmws.dto;

import java.util.List;

public class BoardResponse {
  private Long id;
  private String title;
  private String background;
  private List<BoardColumnResponse> columns;
  private List<UserSummaryResponse> members;

  // Getter & Setter
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

  public String getBackground() {
    return background;
  }

  public void setBackground(String background) {
    this.background = background;
  }

  public List<BoardColumnResponse> getColumns() {
    return columns;
  }

  public void setColumns(List<BoardColumnResponse> columns) {
    this.columns = columns;
  }

  public List<UserSummaryResponse> getMembers() {
    return members;
  }

  public void setMembers(List<UserSummaryResponse> members) {
    this.members = members;
  }
}
