/**
 *  Diese Klasse definiert das Repository für die User-Entität.
 */
package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  /**
   * Diese Methode ermöglicht das Abrufen eines Benutzers anhand seiner E-Mail-Adresse.
   *
   * @param email Die E-Mail-Adresse des Benutzers, der abgerufen werden soll.
   * @return Ein Optional, das den Benutzer enthält, wenn er gefunden wird, oder leer ist, wenn er nicht gefunden wird.
   */
  @Query("SELECT u FROM User u WHERE u.email = :email")
  Optional<User> findByEmail(@Param("email") String email);

  /**
   * Diese Methode ermöglicht die Überprüfung, ob ein Benutzer mit einer bestimmten E-Mail-Adresse bereits existiert.
   *
   * @param email Die E-Mail-Adresse, die überprüft werden soll.
   * @return true, wenn ein Benutzer mit der angegebenen E-Mail-Adresse existiert, andernfalls false.
   */
  @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
  boolean existsByEmail(@Param("email") String email);
}
