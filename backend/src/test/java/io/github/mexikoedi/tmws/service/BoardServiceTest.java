/**
 * Diese Klasse enthält Unit-Tests für die BoardService-Klasse, um sicherzustellen, dass die
 * Geschäftslogik korrekt funktioniert. Es werden verschiedene Szenarien getestet, einschließlich
 * erfolgreicher Operationen und Fehlerfälle. Mockito wird verwendet, um die Abhängigkeiten zu
 * mocken und die Interaktionen zu überprüfen. JUnit 5 wird als Testframework verwendet, um die
 * Tests zu strukturieren und auszuführen. Die Tests decken Funktionen wie das Auflisten von Boards,
 * das Erstellen von Boards, das Einladen von Benutzern, das Hinzufügen und Verschieben von Spalten
 * sowie das Hinzufügen, Aktualisieren, Verschieben und Löschen von Aufgaben ab. Jeder Test
 * überprüft die erwarteten Ergebnisse und die Interaktionen mit den gemockten Abhängigkeiten, um
 * sicherzustellen, dass die BoardService-Klasse wie erwartet funktioniert.
 */
package io.github.mexikoedi.tmws.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.mexikoedi.tmws.model.*;
import io.github.mexikoedi.tmws.repository.*;
import io.github.mexikoedi.tmws.service.dto.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService Tests")
class BoardServiceTest {
  @Mock private BoardRepository boardRepository;
  @Mock private BoardColumnRepository columnRepository;
  @Mock private TaskRepository taskRepository;
  @Mock private UserRepository userRepository;
  @Mock private EmailService emailService;
  @Mock private WebSocketNotificationService websocket;
  @InjectMocks private BoardService boardService;
  private User user;
  private Board board;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setEnabled(true);
    board = new Board();
    board.setId(10L);
    board.setTitle("Test Projektboard");
    board.setOwner(user);
    board.setMembers(new HashSet<>(List.of(user)));
    board.setColumns(new HashSet<>());
  }

  @Test
  @DisplayName("listBoards() - Sollte die Boards des Benutzers zurückgeben.")
  void listBoards_success() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(boardRepository.findBoardsForUserWithRelations(user)).thenReturn(List.of(board));
    List<BoardResponse> result = boardService.listBoards("test@example.com");
    assertEquals(1, result.size());
    assertEquals("Test Projektboard", result.getFirst().getTitle());
  }

  @Test
  @DisplayName(
      "createBoard() - Sollte ein neues Board erstellen und die Standardspalten hinzufügen.")
  void createBoard_success() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    when(boardRepository.save(any(Board.class)))
        .thenAnswer(
            inv -> {
              Board b = inv.getArgument(0);
              b.setId(99L);

              return b;
            });

    Board newBoard = new Board();
    newBoard.setTitle("Neues Projektboard");
    newBoard.setColumns(new HashSet<>());
    newBoard.setMembers(new HashSet<>());
    Board saved = boardService.createBoard(newBoard, "test@example.com");
    assertEquals(3, saved.getColumns().size());
    verify(boardRepository).save(saved);
    verify(websocket).notifyBoardAndMembers(saved);
  }

  @Test
  @DisplayName(
      "invite() - Sollte einen Benutzer zum Board einladen und eine Benachrichtigungs-E-Mail"
          + " senden.")
  void invite_success() {
    User invited = new User();
    invited.setId(2L);
    invited.setEmail("invite@example.com");
    invited.setEnabled(true);
    when(boardRepository.findByIdWithRelations(10L)).thenReturn(Optional.of(board));
    when(userRepository.findByEmail("invite@example.com")).thenReturn(Optional.of(invited));
    when(boardRepository.save(board)).thenReturn(board);
    InviteBoardRequest req = new InviteBoardRequest("invite@example.com");
    Board result = boardService.invite(10L, req);
    assertTrue(result.getMembers().contains(invited));
    verify(emailService).sendAccountInvitedEmail("invite@example.com", "Test Projektboard");
    verify(websocket).notifyBoardAndMembers(board);
  }

  @Test
  @DisplayName(
      "addColumn() - Sollte eine neue Spalte zum Board hinzufügen und die Mitglieder"
          + " benachrichtigen.")
  void addColumn_success() {
    when(boardRepository.findByIdWithRelations(10L)).thenReturn(Optional.of(board));

    when(columnRepository.save(any(BoardColumn.class)))
        .thenAnswer(
            inv -> {
              BoardColumn c = inv.getArgument(0);
              c.setId(5L);

              return c;
            });

    UpdateBoardColumnRequest req = new UpdateBoardColumnRequest("Neue Spalte");
    BoardColumnResponse response = boardService.addColumn(10L, req);
    assertEquals("Neue Spalte", response.getTitle());
    verify(websocket).notifyBoardAndMembers(board);
  }

  @Test
  @DisplayName(
      "moveColumn() - Sollte die Spalte an die neue Position verschieben und die Positionen der"
          + " anderen Spalten entsprechend anpassen.")
  void moveColumn_success() {
    BoardColumn c1 = new BoardColumn();
    c1.setId(1L);
    c1.setPosition(0);
    BoardColumn c2 = new BoardColumn();
    c2.setId(2L);
    c2.setPosition(1);
    BoardColumn c3 = new BoardColumn();
    c3.setId(3L);
    c3.setPosition(2);
    board.setColumns(new HashSet<>(List.of(c1, c2, c3)));
    when(columnRepository.findByIdWithRelations(2L)).thenReturn(Optional.of(c2));
    c2.setBoard(board);
    boardService.moveColumn(2L, 0);
    assertEquals(0, c2.getPosition());
    assertEquals(1, c1.getPosition());
    assertEquals(2, c3.getPosition());
    verify(boardRepository).save(board);
  }

  @Test
  @DisplayName(
      "deleteColumn() - Soll die Spalte löschen, alle darin enthaltenen Aufgaben entfernen und die"
          + " Mitglieder benachrichtigen.")
  void deleteColumn_success() {
    BoardColumn col = new BoardColumn();
    col.setId(5L);
    col.setBoard(board);
    col.setTasks(new HashSet<>());
    board.getColumns().add(col);
    when(columnRepository.findByIdWithRelations(5L)).thenReturn(Optional.of(col));
    boardService.deleteColumn(5L);
    verify(taskRepository).deleteAll(col.getTasks());
    verify(columnRepository).delete(col);
    verify(websocket).notifyBoardAndMembers(board);
  }

  @Test
  @DisplayName(
      "addTask() - Soll eine neue Aufgabe zur Spalte hinzufügen, die Mitglieder benachrichtigen und"
          + " die Aufgabe zurückgeben.")
  void addTask_success() {
    BoardColumn col = new BoardColumn();
    col.setId(5L);
    col.setBoard(board);
    col.setTasks(new HashSet<>());
    when(columnRepository.findByIdWithRelations(5L)).thenReturn(Optional.of(col));

    when(taskRepository.save(any(Task.class)))
        .thenAnswer(
            inv -> {
              Task t = inv.getArgument(0);
              t.setId(100L);

              return t;
            });

    UpdateTaskRequest req = new UpdateTaskRequest("Aufgabe 1", null, null, null, null, null);
    TaskResponse res = boardService.addTask(5L, req);
    assertEquals("Aufgabe 1", res.getTitle());
    verify(websocket).notifyBoardAndMembers(board);
  }

  @Test
  @DisplayName(
      "updateTask() - Soll die Aufgabe aktualisieren, die Mitglieder benachrichtigen und die"
          + " aktualisierte Aufgabe zurückgeben.")
  void updateTask_success() {
    BoardColumn col = new BoardColumn();
    col.setBoard(board);
    Task task = new Task();
    task.setId(10L);
    task.setColumn(col);
    task.setAssignees(new HashSet<>());
    when(taskRepository.findByIdWithRelations(10L)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);
    UpdateTaskRequest req =
        new UpdateTaskRequest("Neuer Titel", "Beschreibung", null, null, null, null);
    Task updated = boardService.updateTask(10L, req);
    assertEquals("Neuer Titel", updated.getTitle());
    assertEquals("Beschreibung", updated.getDescription());
    verify(websocket).notifyBoardAndMembers(board);
  }

  @Test
  @DisplayName(
      "moveTask() - Soll die Aufgabe in die neue Spalte verschieben, die Positionen der anderen"
          + " Aufgaben entsprechend anpassen, die Mitglieder benachrichtigen und die aktualisierte"
          + " Aufgabe zurückgeben.")
  void moveTask_success() {
    BoardColumn oldCol = new BoardColumn();
    oldCol.setId(1L);
    oldCol.setBoard(board);
    oldCol.setTasks(new HashSet<>());
    BoardColumn newCol = new BoardColumn();
    newCol.setId(2L);
    newCol.setBoard(board);
    newCol.setTasks(new HashSet<>());
    Task task = new Task();
    task.setId(10L);
    task.setTitle("Test Aufgabe");
    task.setPosition(0);
    task.setColumn(oldCol);
    when(taskRepository.findTaskWithColumn(10L)).thenReturn(Optional.of(task));
    when(columnRepository.findColumnWithTasks(2L)).thenReturn(Optional.of(newCol));
    when(taskRepository.save(task)).thenReturn(task);
    TaskResponse res = boardService.moveTask(10L, 2L, 0);
    assertEquals(2L, task.getColumn().getId());
    assertEquals(0, task.getPosition());
    assertNotNull(res);
    assertEquals(10L, res.getId());
    assertEquals("Test Aufgabe", res.getTitle());
    assertEquals(0, res.getPosition());
    verify(websocket).notifyBoardAndMembers(board);
  }

  @Test
  @DisplayName(
      "deleteTask() - Soll die Aufgabe löschen, die Mitglieder benachrichtigen und sicherstellen,"
          + " dass die Aufgabe aus der Spalte entfernt wird.")
  void deleteTask_success() {
    BoardColumn col = new BoardColumn();
    col.setBoard(board);
    col.setTasks(new HashSet<>());
    Task task = new Task();
    task.setId(10L);
    task.setColumn(col);
    col.getTasks().add(task);
    when(taskRepository.findByIdWithRelations(10L)).thenReturn(Optional.of(task));
    boardService.deleteTask(10L);
    verify(taskRepository).delete(task);
    verify(websocket).notifyBoardAndMembers(board);
  }
}
