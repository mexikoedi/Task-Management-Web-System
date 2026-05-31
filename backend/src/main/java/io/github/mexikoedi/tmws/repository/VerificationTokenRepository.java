/**
 * Diese Klasse definiert das Repository für die VerificationToken-Entität.
 */
package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.model.VerificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
  /**
   * Diese Methode ermöglicht das Abrufen eines VerificationToken anhand seines Tokens und lädt dabei den
   * zugehörigen Benutzer vorab.
   *
   * @param token Der Token, für den der VerificationToken abgerufen werden soll.
   * @return Ein Optional, das den VerificationToken mit dem zugehörigen Benutzer enthält, wenn er gefunden wird,
   * oder leer ist, wenn er nicht gefunden wird.
   */
  @Query("""
    SELECT t FROM VerificationToken t
    JOIN FETCH t.user
    WHERE t.token = :token
    """)
  Optional<VerificationToken> findByToken(@Param("token") String token);

  /**
   * Diese Methode ermöglicht das Löschen aller VerificationToken, die einem bestimmten Benutzer zugeordnet sind.
   *
   * @param user Der Benutzer, für den die VerificationToken gelöscht werden sollen.
   */
  @Modifying
  @Query("DELETE FROM VerificationToken t WHERE t.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
