/**
 * Diese Klasse implementiert die Geschäftslogik für die Verwaltung von Projektboards,
 * Statuskategorien und Aufgaben.
 */
package io.github.mexikoedi.tmws.service;

import io.github.mexikoedi.tmws.model.Board;
import io.github.mexikoedi.tmws.model.BoardColumn;
import io.github.mexikoedi.tmws.model.Task;
import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.repository.BoardColumnRepository;
import io.github.mexikoedi.tmws.repository.BoardRepository;
import io.github.mexikoedi.tmws.repository.TaskRepository;
import io.github.mexikoedi.tmws.repository.UserRepository;
import java.util.*;
import java.util.stream.Collectors;
import io.github.mexikoedi.tmws.service.dto.*;
import io.github.mexikoedi.tmws.service.exception.*;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {
  private final BoardRepository boardRepository;
  private final BoardColumnRepository columnRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final EmailService emailService;
  private final WebSocketNotificationService websocket;

  /**
   * Konstruktor für AuthService, der alle benötigten Abhängigkeiten injiziert.
   *
   * @param boardRepository Repository für Board-Entitäten.
   * @param columnRepository Repository für BoardColumn-Entitäten.
   * @param taskRepository Repository für Task-Entitäten.
   * @param userRepository Repository für User-Entitäten.
   * @param emailService Service für das Versenden von E-Mails.
   * @param websocket Service für das Versenden von WebSocket-Benachrichtigungen.
   */
  public BoardService(
      BoardRepository boardRepository,
      BoardColumnRepository columnRepository,
      TaskRepository taskRepository,
      UserRepository userRepository,
      EmailService emailService,
      WebSocketNotificationService websocket) {
    this.boardRepository = boardRepository;
    this.columnRepository = columnRepository;
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.emailService = emailService;
    this.websocket = websocket;
  }

  /**
   * Gibt eine Liste aller Projektboards zurück, auf die der Benutzer Zugriff hat, einschließlich
   * aller zugehörigen Statuskategorien und Aufgaben.
   *
   * @param email Die E-Mail-Adresse des Benutzers, für den die Projektboards abgerufen werden
   *     sollen.
   * @throws ResourceNotFoundException Wenn kein Benutzer mit der angegebenen E-Mail-Adresse
   *     gefunden wird.
   * @return Eine Liste von BoardResponse-Objekten, die die Projektboards und ihre Details
   *     repräsentieren.
   */
  public List<BoardResponse> listBoards(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden."));

    return boardRepository.findBoardsForUserWithRelations(user).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Gibt ein bestimmtes Projektboard zurück, einschließlich aller zugehörigen Statuskategorien und
   * Aufgaben.
   *
   * @param id Die ID des Projektboards, das abgerufen werden soll.
   * @return Ein Optional, das das Board-Objekt enthält, wenn es gefunden wurde, oder leer ist, wenn
   *     kein Board mit der angegebenen ID existiert.
   */
  public Optional<Board> getBoard(Long id) {
    return boardRepository.findByIdWithRelations(id);
  }

  /**
   * Erstellt ein neues Projektboard mit den angegebenen Details und setzt den angegebenen Benutzer
   * als Besitzer und erstes Mitglied.
   *
   * @param board Die Details des zu erstellenden Projektboards, einschließlich Titel und optionalem
   *     Hintergrund.
   * @param ownerEmail Die E-Mail-Adresse des Benutzers, der als Besitzer des Projektboards
   *     festgelegt werden soll.
   * @throws ResourceNotFoundException Wenn kein Benutzer mit der angegebenen E-Mail-Adresse
   *     gefunden wird.
   * @return Das erstellte Board-Objekt mit allen zugehörigen Details, einschließlich der
   *     automatisch erstellten Statuskategorien.
   */
  @Transactional
  public Board createBoard(Board board, String ownerEmail) {
    User owner =
        userRepository
            .findByEmail(ownerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer existiert nicht."));
    board.setOwner(owner);
    board.getMembers().add(owner);
    board.getColumns().add(createColumn(board, "Demnächst", 0));
    board.getColumns().add(createColumn(board, "In Bearbeitung", 1));
    board.getColumns().add(createColumn(board, "Fertig", 2));
    Board saved = boardRepository.save(board);
    websocket.notifyBoardAndMembers(saved);

    return saved;
  }

  /**
   * Aktualisiert die Details eines bestehenden Projektboards, einschließlich Titel und Hintergrund.
   *
   * @param boardId Die ID des Projektboards, das aktualisiert werden soll.
   * @param request Ein Objekt, das die neuen Details des Projektboards enthält, einschließlich
   *     Titel und Hintergrund.
   * @throws ResourceNotFoundException Wenn kein Projektboard mit der angegebenen ID gefunden wird.
   * @return Das aktualisierte Board-Objekt mit den neuen Details.
   */
  @Transactional
  public Board updateBoard(Long boardId, UpdateBoardRequest request) {
    Board board =
        boardRepository
            .findByIdWithRelations(boardId)
            .orElseThrow(() -> new ResourceNotFoundException("Projektboard nicht gefunden."));
    board.setTitle(request.getTitle());
    board.setBackground(
        (request.getBackground() == null || request.getBackground().isBlank())
            ? null
            : request.getBackground());
    Board saved = boardRepository.save(board);
    websocket.notifyBoardAndMembers(saved);

    return saved;
  }

  /**
   * Lädt einen Benutzer ein, Mitglied eines bestehenden Projektboards zu werden, basierend auf der
   * E-Mail-Adresse des Benutzers.
   *
   * @param boardId Die ID des Projektboards, zu dem der Benutzer eingeladen werden soll.
   * @param request Ein Objekt, das die E-Mail-Adresse des Benutzers enthält, der eingeladen werden
   *     soll.
   * @throws ResourceNotFoundException Wenn kein Projektboard mit der angegebenen ID gefunden wird
   *     oder wenn kein Benutzer mit der angegebenen E-Mail-Adresse gefunden wird.
   * @throws UserDeactivatedException Wenn der Benutzer, der eingeladen werden soll, deaktiviert
   *     ist.
   * @throws UserInviteNotPossibleException Wenn versucht wird, den Besitzer des Projektboards
   *     einzuladen.
   * @throws UserAlreadyMemberException Wenn der Benutzer bereits Mitglied des Projektboards ist.
   * @return Das aktualisierte Board-Objekt, das den neu eingeladenen Benutzer als Mitglied enthält.
   */
  @Transactional
  public Board invite(Long boardId, InviteBoardRequest request) {
    Board board =
        boardRepository
            .findByIdWithRelations(boardId)
            .orElseThrow(() -> new ResourceNotFoundException("Projektboard nicht gefunden."));
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer existiert nicht."));

    if (!user.isEnabled()) {
      throw new UserDeactivatedException("Benutzer ist deaktiviert.");
    }

    if (user.getId().equals(board.getOwner().getId())) {
      throw new UserInviteNotPossibleException("Der Besitzer kann nicht eingeladen werden.");
    }

    if (board.getMembers().contains(user)) {
      throw new UserAlreadyMemberException("Benutzer ist bereits Mitglied.");
    }

    board.getMembers().add(user);
    emailService.sendAccountInvitedEmail(user.getEmail(), board.getTitle());
    Board saved = boardRepository.save(board);
    websocket.notifyBoardAndMembers(board);

    return saved;
  }

  /**
   * Fügt eine neue Statuskategorie zu einem bestehenden Projektboard hinzu, basierend auf dem Titel
   * der Kategorie.
   *
   * @param boardId Die ID des Projektboards, zu dem die Statuskategorie hinzugefügt werden soll.
   * @param request Ein Objekt, das den Titel der neuen Statuskategorie enthält.
   * @throws ResourceNotFoundException Wenn kein Projektboard mit der angegebenen ID gefunden wird.
   * @throws ColumnAlreadyExistsException Wenn bereits eine Statuskategorie mit demselben Titel im
   *     Projektboard existiert.
   * @return Ein BoardColumnResponse-Objekt, das die Details der neu hinzugefügten Statuskategorie
   *     enthält.
   */
  @Transactional
  public BoardColumnResponse addColumn(Long boardId, UpdateBoardColumnRequest request) {
    Board board =
        boardRepository
            .findByIdWithRelations(boardId)
            .orElseThrow(() -> new ResourceNotFoundException("Projektboard nicht gefunden."));
    boolean exists =
        board.getColumns().stream()
            .anyMatch(c -> c.getTitle().equalsIgnoreCase(request.getTitle()));

    if (exists) {
      throw new ColumnAlreadyExistsException("Statuskategorie mit diesem Namen existiert bereits.");
    }

    BoardColumn column = createColumn(board, request.getTitle(), board.getColumns().size());
    BoardColumn saved = columnRepository.save(column);
    board.getColumns().add(saved);
    websocket.notifyBoardAndMembers(board);

    return getBoardColumnResponse(board, saved);
  }

  /**
   * Aktualisiert die Details einer bestehenden Statuskategorie, einschließlich des Titels,
   * basierend auf der ID der Kategorie.
   *
   * @param columnId Die ID der Statuskategorie, die aktualisiert werden soll.
   * @param request Ein Objekt, das die neuen Details der Statuskategorie enthält, einschließlich
   *     des Titels.
   * @throws ResourceNotFoundException Wenn keine Statuskategorie mit der angegebenen ID gefunden
   *     wird.
   * @throws ColumnAlreadyExistsException Wenn bereits eine andere Statuskategorie mit demselben
   *     Titel im Projektboard existiert.
   * @return Das aktualisierte Board-Objekt, das die neuen Details der Statuskategorie enthält.
   */
  @Transactional
  public Board updateColumn(Long columnId, UpdateBoardColumnRequest request) {
    BoardColumn col =
        columnRepository
            .findByIdWithRelations(columnId)
            .orElseThrow(() -> new ResourceNotFoundException("Statuskategorie nicht gefunden."));
    Board board = col.getBoard();

    if (request.getTitle() != null && !request.getTitle().isBlank()) {
      boolean exists =
          board.getColumns().stream()
              .anyMatch(
                  c ->
                      c.getTitle().equalsIgnoreCase(request.getTitle())
                          && !c.getId().equals(columnId));

      if (exists) {
        throw new ColumnAlreadyExistsException(
            "Statuskategorie mit diesem Namen existiert bereits.");
      }

      col.setTitle(request.getTitle().trim());
    }

    websocket.notifyBoardAndMembers(board);

    return board;
  }

  /**
   * Verschiebt eine Statuskategorie innerhalb eines Projektboards an eine neue Position, basierend
   * auf der ID der Kategorie und der neuen Position.
   *
   * @param columnId Die ID der Statuskategorie, die verschoben werden soll.
   * @param position Die neue Position, an die die Statuskategorie verschoben werden soll
   *     (0-basiert).
   * @throws ResourceNotFoundException Wenn keine Statuskategorie mit der angegebenen ID gefunden
   *     wird.
   */
  @Transactional
  public void moveColumn(Long columnId, int position) {
    BoardColumn column =
        columnRepository
            .findByIdWithRelations(columnId)
            .orElseThrow(() -> new ResourceNotFoundException("Statuskategorie nicht gefunden."));
    Board board = column.getBoard();
    List<BoardColumn> columns = new ArrayList<>(board.getColumns());
    columns.sort(Comparator.comparing(BoardColumn::getPosition));
    columns.removeIf(c -> c.getId().equals(columnId));
    int safePos = Math.clamp(position, 0, columns.size());
    columns.add(safePos, column);

    for (int i = 0; i < columns.size(); i++) {
      columns.get(i).setPosition(i);
    }

    boardRepository.save(board);
    websocket.notifyBoardAndMembers(board);
  }

  /**
   * Löscht eine Statuskategorie aus einem Projektboard, basierend auf der ID der Kategorie.
   *
   * @param columnId Die ID der Statuskategorie, die gelöscht werden soll.
   * @throws ResourceNotFoundException Wenn keine Statuskategorie mit der angegebenen ID gefunden
   *     wird.
   */
  @Transactional
  public void deleteColumn(Long columnId) {
    BoardColumn column =
        columnRepository
            .findByIdWithRelations(columnId)
            .orElseThrow(() -> new ResourceNotFoundException("Statuskategorie nicht gefunden."));
    Board board = column.getBoard();
    board.getColumns().remove(column);
    taskRepository.deleteAll(column.getTasks());
    columnRepository.delete(column);
    websocket.notifyBoardAndMembers(board);
  }

  /**
   * Fügt eine neue Aufgabe zu einer bestehenden Statuskategorie hinzu, basierend auf der ID der
   * Kategorie und den Details der Aufgabe.
   *
   * @param columnId Die ID der Statuskategorie, zu der die Aufgabe hinzugefügt werden soll.
   * @param request Ein Objekt, das die Details der zu erstellenden Aufgabe enthält.
   * @throws ResourceNotFoundException Wenn keine Statuskategorie mit der angegebenen ID gefunden
   *     wird.
   * @throws TaskAlreadyExistsException Wenn bereits eine Aufgabe mit demselben Titel in der
   *     Statuskategorie existiert.
   * @return Ein TaskResponse-Objekt, das die Details der neu hinzugefügten Aufgabe enthält.
   */
  @Transactional
  public TaskResponse addTask(Long columnId, UpdateTaskRequest request) {
    BoardColumn column =
        columnRepository
            .findByIdWithRelations(columnId)
            .orElseThrow(() -> new ResourceNotFoundException("Statuskategorie nicht gefunden."));
    Board board = column.getBoard();
    boolean exists =
        board.getColumns().stream()
            .flatMap(c -> c.getTasks().stream())
            .anyMatch(t -> t.getTitle().equalsIgnoreCase(request.getTitle()));

    if (exists) {
      throw new TaskAlreadyExistsException("Aufgabe mit diesem Namen existiert bereits.");
    }

    Task task = new Task();
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setLabels(request.getLabels());
    task.setAttachments(request.getAttachments());
    task.setDeadline(request.getDeadline() != null ? request.getDeadline() : null);
    task.setColumn(column);
    task.setPosition(column.getTasks().size());

    if (request.getAssigneeIds() != null) {
      Set<User> assignees = new HashSet<>(userRepository.findAllById(request.getAssigneeIds()));
      task.setAssignees(assignees);
    }

    Task saved = taskRepository.save(task);
    column.getTasks().add(saved);
    websocket.notifyBoardAndMembers(column.getBoard());

    return TaskResponse.fromEntity(saved);
  }

  /**
   * Aktualisiert die Details einer bestehenden Aufgabe, basierend auf der ID der Aufgabe und den
   * neuen Details.
   *
   * @param taskId Die ID der Aufgabe, die aktualisiert werden soll.
   * @param request Ein Objekt, das die neuen Details der Aufgabe enthält.
   * @throws ResourceNotFoundException Wenn keine Aufgabe mit der angegebenen ID gefunden wird.
   * @throws TaskAlreadyExistsException Wenn bereits eine andere Aufgabe mit demselben Titel in der
   *     Statuskategorie existiert.
   * @return Das aktualisierte Task-Objekt mit den neuen Details.
   */
  @Transactional
  public Task updateTask(Long taskId, UpdateTaskRequest request) {
    Task task =
        taskRepository
            .findByIdWithRelations(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Aufgabe nicht gefunden."));
    Board board = task.getColumn().getBoard();
    boolean exists =
        board.getColumns().stream()
            .flatMap(c -> c.getTasks().stream())
            .anyMatch(
                t ->
                    t.getTitle().equalsIgnoreCase(request.getTitle()) && !t.getId().equals(taskId));

    if (exists) {
      throw new TaskAlreadyExistsException("Aufgabe mit diesem Namen existiert bereits.");
    }

    Set<User> previousAssignees = new HashSet<>(task.getAssignees());
    if (request.getTitle() != null) task.setTitle(request.getTitle());
    if (request.getDescription() != null) task.setDescription(request.getDescription());
    if (request.getLabels() != null) task.setLabels(request.getLabels());
    if (request.getAttachments() != null) task.setAttachments(request.getAttachments());

    if (request.getDeadline() != null) {
      task.setDeadline(request.getDeadline());
    } else {
      task.setDeadline(null);
    }

    Set<User> newAssignees = new HashSet<>();

    if (request.getAssigneeIds() != null) {
      newAssignees = new HashSet<>(userRepository.findAllById(request.getAssigneeIds()));
      task.setAssignees(newAssignees);
    }

    sendAssigneeChangeEmails(task, previousAssignees, newAssignees);
    Task saved = taskRepository.save(task);
    websocket.notifyBoardAndMembers(saved.getColumn().getBoard());

    return saved;
  }

  /**
   * Verschiebt eine Aufgabe von einer Statuskategorie zu einer anderen innerhalb des Projektboards,
   * basierend auf der ID der Aufgabe, der ID der Zielkategorie und der neuen Position innerhalb der
   * Zielkategorie.
   *
   * @param taskId Die ID der Aufgabe, die verschoben werden soll.
   * @param targetColumnId Die ID der Ziel-Statuskategorie, zu der die Aufgabe verschoben werden
   *     soll.
   * @param position Die neue Position, an die die Aufgabe innerhalb der Ziel-Statuskategorie
   *     verschoben werden soll (0-basiert).
   * @throws ResourceNotFoundException Wenn keine Aufgabe mit der angegebenen ID gefunden wird oder
   *     wenn keine Statuskategorie mit der angegebenen Ziel-ID gefunden wird.
   * @return Ein TaskResponse-Objekt, das die Details der verschobenen Aufgabe enthält,
   *     einschließlich der neuen Position und der neuen Statuskategorie.
   */
  @Transactional
  public TaskResponse moveTask(Long taskId, Long targetColumnId, int position) {
    Task task =
        taskRepository
            .findTaskWithColumn(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Aufgabe nicht gefunden."));
    BoardColumn oldColumn = task.getColumn();
    BoardColumn targetColumn =
        columnRepository
            .findColumnWithTasks(targetColumnId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Ziel-Statuskategorie nicht gefunden."));

    if (oldColumn != null && !Objects.equals(oldColumn.getId(), targetColumnId)) {
      oldColumn.getTasks().removeIf(t -> Objects.equals(t.getId(), taskId));
    }

    List<Task> tasks =
        targetColumn.getTasks().stream()
            .filter(t -> !Objects.equals(t.getId(), taskId))
            .sorted(Comparator.comparingInt(Task::getPosition))
            .toList();
    List<Task> mutable = new ArrayList<>(tasks);
    int safePos = Math.clamp(position, 0, mutable.size());
    mutable.add(safePos, task);

    for (int i = 0; i < mutable.size(); i++) {
      mutable.get(i).setPosition(i);
    }

    Set<Task> targetSet = targetColumn.getTasks();
    targetSet.clear();
    targetSet.addAll(mutable);
    task.setColumn(targetColumn);
    task.setPosition(safePos);
    Task saved = taskRepository.save(task);
    websocket.notifyBoardAndMembers(targetColumn.getBoard());

    return TaskResponse.fromEntity(saved);
  }

  /**
   * Löscht eine Aufgabe aus einer Statuskategorie, basierend auf der ID der Aufgabe.
   *
   * @param taskId Die ID der Aufgabe, die gelöscht werden soll.
   * @throws ResourceNotFoundException Wenn keine Aufgabe mit der angegebenen ID gefunden wird.
   */
  @Transactional
  public void deleteTask(Long taskId) {
    Task task =
        taskRepository
            .findByIdWithRelations(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Aufgabe nicht gefunden."));
    BoardColumn column = task.getColumn();
    column.getTasks().removeIf(t -> t.getId().equals(taskId));
    taskRepository.delete(task);
    websocket.notifyBoardAndMembers(column.getBoard());
  }

  /**
   * Hilfsmethode, um ein Board-Objekt in ein BoardResponse-Objekt zu konvertieren.
   *
   * @param board Das Board-Objekt, das in ein BoardResponse-Objekt konvertiert werden soll.
   * @return Ein BoardResponse-Objekt, das die Details des Projektboards und seiner zugehörigen
   *     Statuskategorien, Aufgaben und Mitglieder enthält.
   */
  public BoardResponse mapToResponse(Board board) {
    BoardResponse response = new BoardResponse();
    response.setId(board.getId());
    response.setTitle(board.getTitle());
    response.setBackground(board.getBackground());
    response.setColumns(
        board.getColumns().stream()
            .sorted(Comparator.comparingInt(BoardColumn::getPosition))
            .map(c -> getBoardColumnResponse(board, c))
            .toList());
    Set<User> allMembers = new LinkedHashSet<>(board.getMembers());

    if (board.getOwner().isEnabled()) {
      allMembers.add(board.getOwner());
    }

    response.setMembers(
        allMembers.stream()
            .filter(User::isEnabled)
            .sorted(Comparator.comparingLong(User::getId))
            .map(UserResponse::new)
            .toList());
    response.setOwner(new UserResponse(board.getOwner()));

    return response;
  }

  /**
   * Hilfsmethode, um ein BoardColumn-Objekt in ein BoardColumnResponse-Objekt zu konvertieren.
   *
   * @param board Das Board-Objekt, zu dem die Statuskategorie gehört, um die BoardId im Response
   *     setzen zu können.
   * @param saved Das BoardColumn-Objekt, das in ein BoardColumnResponse-Objekt konvertiert werden
   *     soll.
   * @return Ein BoardColumnResponse-Objekt, das die Details der Statuskategorie und ihrer
   *     zugehörigen Aufgaben enthält.
   */
  @NonNull
  private BoardColumnResponse getBoardColumnResponse(Board board, BoardColumn saved) {
    BoardColumnResponse response = new BoardColumnResponse();
    response.setId(saved.getId());
    response.setTitle(saved.getTitle());
    response.setPosition(saved.getPosition());
    response.setBoardId(board.getId());
    response.setTasks(
        saved.getTasks().stream()
            .sorted(Comparator.comparingInt(Task::getPosition))
            .map(TaskResponse::fromEntity)
            .collect(Collectors.toList()));

    return response;
  }

  /**
   * Hilfsmethode, um eine neue Statuskategorie mit den angegebenen Details zu erstellen und sie
   * einem Projektboard zuzuordnen.
   *
   * @param board Das Board-Objekt, dem die neue Statuskategorie zugeordnet werden soll.
   * @param title Der Titel der neuen Statuskategorie.
   * @param position Die Position der neuen Statuskategorie innerhalb des Projektboards (0-basiert).
   * @return Ein BoardColumn-Objekt, das die Details der neu erstellten Statuskategorie enthält und
   *     mit dem Projektboard verknüpft ist.
   */
  private BoardColumn createColumn(Board board, String title, int position) {
    BoardColumn c = new BoardColumn();
    c.setTitle(title);
    c.setPosition(position);
    c.setBoard(board);

    return c;
  }

  /**
   * Hilfsmethode, um E-Mails an Benutzer zu senden, wenn sich die Zuweisung einer Aufgabe ändert.
   *
   * @param task Die Aufgabe, deren Zuweisung sich geändert hat, um die Details für die E-Mail zu
   *     verwenden.
   * @param previous Die vorherige Menge von Benutzern, die der Aufgabe zugewiesen waren, um zu
   *     bestimmen, welche Benutzer eine E-Mail über die Entfernung erhalten sollen.
   * @param current Die aktuelle Menge von Benutzern, die der Aufgabe zugewiesen sind, um zu
   *     bestimmen, welche Benutzer eine E-Mail über die neue Zuweisung erhalten sollen.
   */
  private void sendAssigneeChangeEmails(Task task, Set<User> previous, Set<User> current) {
    String boardName = task.getColumn().getBoard().getTitle();
    String statusCategory = task.getColumn().getTitle();
    String taskTitle = task.getTitle();

    for (User u : current) {
      if (!previous.contains(u)) {
        emailService.sendAccountAssignedEmail(u.getEmail(), boardName, statusCategory, taskTitle);
      }
    }

    for (User u : previous) {
      if (!current.contains(u)) {
        emailService.sendAccountUnassignedEmail(u.getEmail(), boardName, statusCategory, taskTitle);
      }
    }
  }
}
