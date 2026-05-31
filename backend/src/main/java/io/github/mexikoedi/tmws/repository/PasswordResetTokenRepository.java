/** Diese Klasse definiert das Repository für die PasswordResetToken-Entität. */
package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.PasswordResetToken;
import io.github.mexikoedi.tmws.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  /**
   * Diese Methode ermöglicht das Abrufen eines PasswordResetToken anhand seines Tokens und lädt
   * dabei den zugehörigen Benutzer vorab.
   *
   * @param token Der Token, für den der PasswordResetToken abgerufen werden soll.
   * @return Ein Optional, das den PasswordResetToken mit dem zugehörigen Benutzer enthält, wenn er
   *     gefunden wird, oder leer ist, wenn er nicht gefunden wird.
   */
  @Query(
      """
      SELECT t FROM PasswordResetToken t
      JOIN FETCH t.user
      WHERE t.token = :token
      """)
  Optional<PasswordResetToken> findByToken(@Param("token") String token);

  /**
   * Diese Methode ermöglicht das Löschen aller PasswordResetToken, die einem bestimmten Benutzer
   * zugeordnet sind.
   *
   * @param user Der Benutzer, für den die PasswordResetToken gelöscht werden sollen.
   */
  @Modifying
  @Query("DELETE FROM PasswordResetToken t WHERE t.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
