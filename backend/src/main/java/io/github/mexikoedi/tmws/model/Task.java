package io.github.mexikoedi.tmws.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private LocalDateTime deadline;

  @ManyToMany
  @JoinTable(
      name = "task_assignees",
      joinColumns = @JoinColumn(name = "task_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  @JsonIgnore
  private Set<User> assignees = new HashSet<>();

  @Column(name = "position_index")
  private int position = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "column_id")
  @JsonBackReference
  private BoardColumn column;

  // simple labels/attachments stored as comma separated values for now
  private String labels;
  private String attachments;

  // getters / setters
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

  public LocalDateTime getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDateTime deadline) {
    this.deadline = deadline;
  }

  public Set<User> getAssignees() {
    return assignees;
  }

  public void setAssignees(Set<User> assignees) {
    this.assignees = assignees;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public BoardColumn getColumn() {
    return column;
  }

  public void setColumn(BoardColumn column) {
    this.column = column;
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
}
