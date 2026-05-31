/**
 * Diese Klasse ist der REST-Controller für die Board-bezogenen Endpunkte der TMWS-Anwendung.
 */
package io.github.mexikoedi.tmws.controller;

import io.github.mexikoedi.tmws.dto.*;
import io.github.mexikoedi.tmws.model.Board;
import io.github.mexikoedi.tmws.model.Task;
import io.github.mexikoedi.tmws.service.BoardService;
import java.util.List;
import java.util.Objects;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
  private final BoardService boardService;

  /**
   * Konstruktor für BoardController, der die BoardService-Instanz injiziert bekommt.
   *
   * @param boardService Die Service-Instanz, die die Geschäftslogik für Board- und Task-Verwaltung enthält.
   */
  public BoardController(BoardService boardService) {
    this.boardService = boardService;
  }

  /**
   * GET /api/boards - Liste alle Boards, auf die der authentifizierte User Zugriff hat.
   *
   * @return Eine ResponseEntity mit einer Liste von BoardResponse-Objekten, die die Boards repräsentieren.
   */
  @GetMapping
  public ResponseEntity<List<BoardResponse>> listBoards() {
    String email = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

    return ResponseEntity.ok(boardService.listBoards(email));
  }

  /**
   * GET /api/boards/{id} - Gibt die Details eines bestimmten Boards zurück, einschließlich seiner Spalten und Aufgaben.
   *
   * @param id Die ID des Boards, das abgerufen werden soll.
   * @return Eine ResponseEntity mit einem BoardResponse-Objekt, das die Details des angeforderten Boards enthält.
   */
  @GetMapping("/{id}")
  public ResponseEntity<BoardResponse> getBoard(@PathVariable Long id) {
    return boardService.getBoard(id).map(boardService::mapToResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /**
   * POST /api/boards - Erstellt ein neues Board basierend auf den übergebenen Daten.
   *
   * @param board Die Board-Entität, die die Informationen für das zu erstellende Board enthält.
   * @return Eine ResponseEntity mit einem BoardResponse-Objekt, das die Details des neu erstellten Boards enthält.
   */
  @PostMapping
  public ResponseEntity<BoardResponse> createBoard(@RequestBody Board board) {
    String email = (String) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    Board created = boardService.createBoard(board, email);

    return ResponseEntity.ok(boardService.mapToResponse(created));
  }

  /**
   * PUT /api/boards/{id} - Aktualisiert die Informationen eines bestehenden Boards basierend auf den übergebenen Daten.
   *
   * @param id Die ID des Boards, das aktualisiert werden soll.
   * @param request Ein UpdateBoardRequest-Objekt, das die neuen Informationen für das Board enthält.
   * @return Eine ResponseEntity mit einem BoardResponse-Objekt, das die Details des aktualisierten Boards enthält.
   */
  @PutMapping("/{id}")
  public ResponseEntity<BoardResponse> updateBoard(@PathVariable Long id, @Valid @RequestBody UpdateBoardRequest request) {
    Board updated = boardService.updateBoard(id, request);

    return ResponseEntity.ok(boardService.mapToResponse(updated));
  }

  /**
   * POST /api/boards/{id}/invite - Lädt einen Benutzer ein, an einem bestehenden Board teilzunehmen, basierend auf den
   * übergebenen Daten.
   *
   * @param id Die ID des Boards, zu dem der Benutzer eingeladen werden soll.
   * @param request Ein InviteBoardRequest-Objekt, das die Informationen für die Einladung enthält.
   * @return Eine ResponseEntity mit einem BoardResponse-Objekt, das die Details des Boards enthält, zu dem der Benutzer
   * eingeladen wurde.
   */
  @PostMapping("/{id}/invite")
  public ResponseEntity<BoardResponse> invite(@PathVariable Long id,  @Valid @RequestBody InviteBoardRequest request) {
    Board updated = boardService.invite(id, request);

    return ResponseEntity.ok(boardService.mapToResponse(updated));
  }

  /**
   * POST /api/boards/{id}/columns - Fügt eine neue Spalte zu einem bestehenden Board hinzu, basierend auf den
   * übergebenen Daten.
   *
   * @param id Die ID des Boards, zu dem die neue Spalte hinzugefügt werden soll.
   * @param request Ein UpdateBoardColumnRequest-Objekt, das die Informationen für die zu erstellende Spalte enthält.
   * @return Eine ResponseEntity mit einem BoardColumnResponse-Objekt, das die Details der neu erstellten Spalte enthält.
   */
  @PostMapping("/{id}/columns")
  public ResponseEntity<BoardColumnResponse> addColumn(@PathVariable Long id, @Valid @RequestBody UpdateBoardColumnRequest request) {
    BoardColumnResponse response = boardService.addColumn(id, request);

    return ResponseEntity.ok(response);
  }

  /**
   * PUT /api/boards/columns/{id} - Aktualisiert die Informationen einer bestehenden Spalte basierend auf den
   * übergebenen Daten.
   *
   * @param id Die ID der Spalte, die aktualisiert werden soll.
   * @param request Ein UpdateBoardColumnRequest-Objekt, das die neuen Informationen für die Spalte enthält.
   * @return Eine ResponseEntity mit einem BoardResponse-Objekt, das die Details des Boards enthält, zu dem
   * die aktualisierte Spalte gehört.
   */
  @PutMapping("/columns/{id}")
  public ResponseEntity<BoardResponse> updateColumn(@PathVariable Long id, @Valid @RequestBody UpdateBoardColumnRequest request) {
    Board updatedBoard = boardService.updateColumn(id, request);

    return ResponseEntity.ok(boardService.mapToResponse(updatedBoard));
  }

  /**
   * PUT /api/boards/columns/{id}/move - Verschiebt eine bestehende Spalte an eine neue Position innerhalb des
   * Boards basierend auf den übergebenen Parametern.
   *
   * @param columnId Die ID der Spalte, die verschoben werden soll.
   * @param position Die neue Position der Spalte innerhalb des Boards, basierend auf einem 0-basierten Index.
   * @return Eine ResponseEntity mit einem leeren Body, die den Erfolg der Verschiebungsoperation anzeigt.
   */
  @PutMapping("/columns/{columnId}/move")
  public ResponseEntity<Void> moveColumn(@PathVariable Long columnId, @RequestParam int position) {
    boardService.moveColumn(columnId, position);

    return ResponseEntity.ok().build();
  }

  /**
   * DELETE /api/boards/columns/{id} - Löscht eine bestehende Spalte basierend auf der übergebenen ID.
   *
   * @param columnId Die ID der Spalte, die gelöscht werden soll.
   * @return Eine ResponseEntity mit einem leeren Body, die den Erfolg der Löschoperation anzeigt.
   */
  @DeleteMapping("/columns/{columnId}")
  public ResponseEntity<Void> deleteColumn(@PathVariable Long columnId) {
    boardService.deleteColumn(columnId);

    return ResponseEntity.noContent().build();
  }

  /**
   * POST /api/boards/columns/{columnId}/tasks - Fügt eine neue Aufgabe zu einer bestehenden Spalte hinzu, basierend
   * auf den übergebenen Daten.
   *
   * @param columnId Die ID der Spalte, zu der die neue Aufgabe hinzugefügt werden soll.
   * @param request Ein UpdateTaskRequest-Objekt, das die Informationen für die zu erstellende Aufgabe enthält.
   * @return Eine ResponseEntity mit einem TaskResponse-Objekt, das die Details der neu erstellten Aufgabe enthält.
   */
  @PostMapping("/columns/{columnId}/tasks")
  public ResponseEntity<TaskResponse> addTask(@PathVariable Long columnId, @Valid @RequestBody UpdateTaskRequest request) {
    TaskResponse saved = boardService.addTask(columnId, request);

    return ResponseEntity.ok(saved);
  }

  /**
   * PUT /api/boards/tasks/{taskId} - Aktualisiert die Informationen einer bestehenden Aufgabe basierend
   * auf den übergebenen Daten.
   *
   * @param taskId Die ID der Aufgabe, die aktualisiert werden soll.
   * @param request Ein UpdateTaskRequest-Objekt, das die neuen Informationen für die Aufgabe enthält.
   * @return Eine ResponseEntity mit einem TaskResponse-Objekt, das die Details der aktualisierten Aufgabe enthält.
   */
  @PutMapping("/tasks/{taskId}")
  public ResponseEntity<TaskResponse> updateTask(@PathVariable Long taskId, @Valid @RequestBody UpdateTaskRequest request) {
    Task updated = boardService.updateTask(taskId, request);

    return ResponseEntity.ok(TaskResponse.fromEntity(updated));
  }

  /**
   * PUT /api/boards/tasks/{taskId}/move - Verschiebt eine bestehende Aufgabe an eine neue Position innerhalb einer
   * Spalte oder zwischen Spalten basierend auf den übergebenen Parametern.
   *
   * @param taskId Die ID der Aufgabe, die verschoben werden soll.
   * @param targetColumnId Die ID der Spalte, zu der die Aufgabe verschoben werden soll.
   * @param position Die neue Position der Aufgabe innerhalb der Zielspalte, basierend auf einem 0-basierten Index.
   * @return Eine ResponseEntity mit einem TaskResponse-Objekt, das die Details der verschobenen Aufgabe enthält.
   */
  @PutMapping("/tasks/{taskId}/move")
  public ResponseEntity<TaskResponse> moveTask(@PathVariable Long taskId, @RequestParam Long targetColumnId, @RequestParam int position) {
    TaskResponse moved = boardService.moveTask(taskId, targetColumnId, position);

    return ResponseEntity.ok(moved);
  }

  /**
   * DELETE /api/boards/tasks/{taskId} - Löscht eine bestehende Aufgabe basierend auf der übergebenen ID.
   *
   * @param taskId Die ID der Aufgabe, die gelöscht werden soll.
   * @return Eine ResponseEntity mit einem leeren Body, die den Erfolg der Löschoperation anzeigt.
   */
  @DeleteMapping("/tasks/{taskId}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
    boardService.deleteTask(taskId);

    return ResponseEntity.noContent().build();
  }
}
