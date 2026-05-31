/**
 * Diese Klasse definiert das Repository für die Task-Entität.
 */
package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.Task;
import io.github.mexikoedi.tmws.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
  /**
   * Diese Methode ermöglicht das Abrufen aller Aufgaben, bei denen der angegebene Benutzer als Assignee zugeordnet
   * ist, und lädt dabei alle zugehörigen Beziehungen (Spalten, Boards und Assignees) vorab.
   *
   * @param user Der Benutzer, für den die Aufgaben abgerufen werden sollen.
   * @return Eine Liste von Aufgaben, bei denen der Benutzer als Assignee zugeordnet ist, einschließlich
   * aller zugehörigen
   */
  @Query("""
    SELECT DISTINCT t FROM Task t
    JOIN FETCH t.assignees a
    JOIN FETCH t.column c
    JOIN FETCH c.board b
    WHERE a = :user
    """)
  List<Task> findAllTasksFromUser(@Param("user") User user);

  /**
   * Diese Methode ermöglicht das Abrufen einer Aufgabe anhand ihrer ID und lädt dabei alle zugehörigen
   * Beziehungen (Spalten, Boards und Assignees) vorab.
   *
   * @param id Die ID der Aufgabe, die abgerufen werden soll.
   * @return Ein Optional, das die Aufgabe mit ihren Beziehungen enthält, wenn sie gefunden wird, oder leer ist,
   * wenn sie nicht gefunden wird.
   */
  @Query("""
    SELECT DISTINCT t FROM Task t
    LEFT JOIN FETCH t.column c
    LEFT JOIN FETCH c.board b
    LEFT JOIN FETCH b.columns bc
    LEFT JOIN FETCH bc.tasks tt
    LEFT JOIN FETCH tt.assignees aa
    LEFT JOIN FETCH t.assignees a
    WHERE t.id = :id
    """)
  Optional<Task> findByIdWithRelations(@Param("id") Long id);

  /**
   * Diese Methode ermöglicht das Abrufen einer Aufgabe anhand ihrer ID und lädt dabei die zugehörige Spalte und
   * die zugeordneten Benutzer vorab, ohne die übergeordneten Boards und deren Spalten und Aufgaben zu laden.
   *
   * @param id Die ID der Aufgabe, die abgerufen werden soll.
   * @return Ein Optional, das die Aufgabe mit ihrer Spalte und den zugeordneten Benutzern enthält, wenn sie
   * gefunden wird, oder leer ist, wenn sie nicht gefunden wird.
   */
  @Query("""
    SELECT t FROM Task t
    LEFT JOIN FETCH t.assignees a
    LEFT JOIN FETCH t.column c
    WHERE t.id = :id
    """)
  Optional<Task> findTaskWithColumn(@Param("id") Long id);
}
