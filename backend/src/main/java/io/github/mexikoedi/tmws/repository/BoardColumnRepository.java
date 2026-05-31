/**
 * Diese Klasse definiert das Repository für die BoardColumn-Entität.
 */
package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
  /**
   * Diese Methode ermöglicht das Abrufen einer BoardColumn mit ihren zugehörigen Beziehungen (Board, Spalten,
   * Aufgaben und Assignees) anhand der ID.
   *
   * @param id Die ID der BoardColumn, die abgerufen werden soll.
   * @return Ein Optional, das die BoardColumn mit ihren Beziehungen enthält, wenn sie gefunden wird,
   * oder leer ist, wenn sie nicht gefunden wird.
   */
  @Query("""
    SELECT DISTINCT c FROM BoardColumn c
    LEFT JOIN FETCH c.board b
    LEFT JOIN FETCH b.columns bc
    LEFT JOIN FETCH bc.tasks t
    LEFT JOIN FETCH t.assignees a
    WHERE c.id = :id
    """)
  Optional<BoardColumn> findByIdWithRelations(@Param("id") Long id);

  /**
   * Diese Methode ermöglicht das Abrufen einer BoardColumn mit ihren zugehörigen Aufgaben und Assignees anhand der ID.
   *
   * @param id Die ID der BoardColumn, die abgerufen werden soll.
   * @return Ein Optional, das die BoardColumn mit ihren Aufgaben und Assignees enthält, wenn sie gefunden wird,
   * oder leer ist, wenn sie nicht gefunden wird.
   */
  @Query("""
    SELECT c FROM BoardColumn c
    LEFT JOIN FETCH c.tasks t
    LEFT JOIN FETCH t.assignees a
    WHERE c.id = :id
    """)
  Optional<BoardColumn> findColumnWithTasks(@Param("id") Long id);
}
