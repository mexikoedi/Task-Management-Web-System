/** Diese Klasse repräsentiert eine Aufgabe im TMWS. */
package io.github.mexikoedi.tmws.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

  private LocalDate deadline;

  @Column(name = "position_index")
  private int position = 0;

  private String labels;

  private String attachments;

  @ManyToMany
  @JoinTable(
      name = "task_assignees",
      joinColumns = @JoinColumn(name = "task_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  @JsonIgnore
  private Set<User> assignees = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "column_id")
  @JsonBackReference
  private BoardColumn column;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Setzt die Erstellungs- und Aktualisierungszeitstempel, bevor die Aufgabe in die Datenbank
   * eingefügt wird.
   */
  @PrePersist
  private void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  /**
   * Aktualisiert den Aktualisierungszeitstempel, bevor die Aufgabe in der Datenbank aktualisiert
   * wird.
   */
  @PreUpdate
  private void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
