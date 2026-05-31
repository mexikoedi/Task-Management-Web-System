/**
 * Diese Klasse repräsentiert ein Passwort-Reset-Token im TMWS.
 */
package io.github.mexikoedi.tmws.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
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
   * Setzt die Erstellungs- und Aktualisierungszeitstempel, bevor das Passwort-Reset-Token in die Datenbank
   * eingefügt wird.
   */
  @PrePersist
  private void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  /**
   * Aktualisiert den Aktualisierungszeitstempel, bevor das Passwort-Reset-Token in der Datenbank aktualisiert wird.
   */
  @PreUpdate
  private void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
