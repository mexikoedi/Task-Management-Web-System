/** Diese Klasse repräsentiert eine Statuskategorie im Projekttboard im TMWS. */
package io.github.mexikoedi.tmws.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "board_columns")
public class BoardColumn {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private int position = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id")
  @JsonBackReference
  private Board board;

  @OneToMany(mappedBy = "column", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("position asc")
  @JsonManagedReference
  private Set<Task> tasks = new LinkedHashSet<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Setzt die Erstellungs- und Aktualisierungszeitstempel, bevor die Statuskategorie in die
   * Datenbank eingefügt wird.
   */
  @PrePersist
  private void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  /**
   * Aktualisiert den Aktualisierungszeitstempel, bevor die Statuskategorie in der Datenbank
   * aktualisiert wird.
   */
  @PreUpdate
  private void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
