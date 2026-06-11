/** Diese Klasse repräsentiert ein Projektboard im TMWS. */
package io.github.mexikoedi.tmws.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "boards")
public class Board {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  @JsonIgnore
  private User owner;

  @Column(nullable = false)
  private String title;

  private String background;

  @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("position asc")
  @JsonManagedReference
  private Set<BoardColumn> columns = new LinkedHashSet<>();

  @ManyToMany
  @JoinTable(
      name = "board_members",
      joinColumns = @JoinColumn(name = "board_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  @JsonIgnore
  private Set<User> members = new HashSet<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Setzt die Erstellungs- und Aktualisierungszeitstempel, bevor das Board in die Datenbank
   * eingefügt wird.
   */
  @PrePersist
  private void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  /**
   * Aktualisiert den Aktualisierungszeitstempel, bevor das Board in der Datenbank aktualisiert
   * wird.
   */
  @PreUpdate
  private void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
