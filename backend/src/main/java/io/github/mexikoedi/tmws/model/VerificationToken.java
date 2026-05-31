/** Diese Klasse repräsentiert ein Verifizierungs-Token im TMWS. */
package io.github.mexikoedi.tmws.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "expiry_date", nullable = false)
  private LocalDateTime expiryDate;

  @Column(name = "used", nullable = false)
  private boolean used = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Setzt die Erstellungs- und Aktualisierungszeitstempel, bevor das Verifizierungs-Token in die
   * Datenbank eingefügt wird.
   */
  @PrePersist
  private void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  /**
   * Aktualisiert den Aktualisierungszeitstempel, bevor das Verifizierungs-Token in der Datenbank
   * aktualisiert wird.
   */
  @PreUpdate
  private void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
