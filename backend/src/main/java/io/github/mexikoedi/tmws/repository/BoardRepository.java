/**
 * Diese Klasse definiert das Repository für die Board-Entität.
 */
package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.Board;
import io.github.mexikoedi.tmws.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
  /**
   * Diese Methode ermöglicht das Abrufen aller Boards, bei denen der angegebene Benutzer entweder Eigentümer
   * oder Mitglied ist, und lädt dabei alle zugehörigen Beziehungen (Spalten, Aufgaben, Assignees, Mitglieder
   * und Eigentümer) vorab.
   *
   * @param user Der Benutzer, für den die Boards abgerufen werden sollen.
   * @return Eine Liste von Boards, bei denen der Benutzer entweder Eigentümer oder Mitglied ist, einschließlich
   * aller zugehörigen Beziehungen.
   */
  @Query("""
    SELECT DISTINCT b FROM Board b
    LEFT JOIN FETCH b.columns c
    LEFT JOIN FETCH c.tasks t
    LEFT JOIN FETCH t.assignees a
    LEFT JOIN FETCH b.members m
    LEFT JOIN FETCH b.owner o
    WHERE b.owner = :user OR m = :user
    """)
  List<Board> findBoardsForUserWithRelations(@Param("user") User user);

  /**
   * Diese Methode ermöglicht das Abrufen eines Boards anhand seiner ID und lädt dabei alle zugehörigen
   * Beziehungen (Spalten, Aufgaben, Assignees, Mitglieder und Eigentümer) vorab.
   *
   * @param id Die ID des Boards, das abgerufen werden soll.
   * @return Ein Optional, das das Board mit seinen Beziehungen enthält, wenn es gefunden wird, oder leer ist,
   * wenn es nicht gefunden wird.
   */
  @Query("""
    SELECT DISTINCT b FROM Board b
    LEFT JOIN FETCH b.columns c
    LEFT JOIN FETCH c.tasks t
    LEFT JOIN FETCH t.assignees a
    LEFT JOIN FETCH b.members m
    LEFT JOIN FETCH b.owner o
    WHERE b.id = :id
    """)
  Optional<Board> findByIdWithRelations(@Param("id") Long id);
}
