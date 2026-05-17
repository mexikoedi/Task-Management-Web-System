package io.github.mexikoedi.tmws.service;

import io.github.mexikoedi.tmws.dto.*;
import io.github.mexikoedi.tmws.exception.EmailAlreadyExistsException;
import io.github.mexikoedi.tmws.exception.ResourceNotFoundException;
import io.github.mexikoedi.tmws.model.Board;
import io.github.mexikoedi.tmws.model.BoardColumn;
import io.github.mexikoedi.tmws.model.Task;
import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.repository.BoardColumnRepository;
import io.github.mexikoedi.tmws.repository.BoardRepository;
import io.github.mexikoedi.tmws.repository.TaskRepository;
import io.github.mexikoedi.tmws.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Service
public class BoardService {
  private final BoardRepository boardRepository;
  private final BoardColumnRepository columnRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final EmailService emailService;
  private final SimpMessagingTemplate messagingTemplate;

  public BoardService(
      BoardRepository boardRepository,
      BoardColumnRepository columnRepository,
      TaskRepository taskRepository,
      UserRepository userRepository,
      EmailService emailService,
      SimpMessagingTemplate messagingTemplate) {
    this.boardRepository = boardRepository;
    this.columnRepository = columnRepository;
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.emailService = emailService;
    this.messagingTemplate = messagingTemplate;
  }

  public List<Board> findAll() {
    return boardRepository.findAll();
  }

  public Optional<Board> findById(Long id) {
    return boardRepository.findById(id);
  }

  @Transactional
  public Board createBoard(Board board, String ownerEmail) {
    User owner = userRepository.findByEmail(ownerEmail).orElse(null);
    if (owner != null) {
      board.setOwner(owner);
      board.getMembers().add(owner);
    }
    // default columns
    BoardColumn c1 = new BoardColumn();
    c1.setTitle("Demnächst");
    c1.setPosition(0);
    c1.setBoard(board);

    BoardColumn c2 = new BoardColumn();
    c2.setTitle("In Bearbeitung");
    c2.setPosition(1);
    c2.setBoard(board);

    BoardColumn c3 = new BoardColumn();
    c3.setTitle("Fertig");
    c3.setPosition(2);
    c3.setBoard(board);

    board.getColumns().add(c1);
    board.getColumns().add(c2);
    board.getColumns().add(c3);

    Board saved = boardRepository.save(board);

    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(saved.getId());

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(saved.getMembers());
    members.add(saved.getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }

    return saved;
  }

  @Transactional
  public Board inviteMember(Long boardId, String email) {

    Board board =
        boardRepository
            .findById(boardId)
            .orElseThrow(() -> new ResourceNotFoundException("Board nicht gefunden"));

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Benutzer existiert nicht"));

    if (board.getMembers().contains(user)) {
      throw new EmailAlreadyExistsException("Benutzer ist bereits Mitglied");
    }

    if (user.getId().equals(board.getOwner().getId())) {
      throw new EmailAlreadyExistsException("Der Besitzer kann nicht eingeladen werden.");
    }

    board.getMembers().add(user);

    emailService.sendAccountInvitedEmail(user.getEmail(), board.getTitle());

    Board saved = boardRepository.save(board);

    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(saved.getId());

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(saved.getMembers());
    members.add(saved.getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }

    return saved;
  }

  @Transactional
  public BoardColumn addColumn(Long boardId, String title) {
    Board board = boardRepository.findById(boardId).orElseThrow();
    BoardColumn column = new BoardColumn();
    column.setTitle(title);
    column.setBoard(board);
    column.setPosition(board.getColumns().size());
    BoardColumn saved = columnRepository.save(column);
    board.getColumns().add(saved);
    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(board.getId());

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(board.getMembers());
    members.add(board.getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }

    return saved;
  }

  @Transactional
  public Task addTask(Long columnId, Task task) {
    BoardColumn column = columnRepository.findById(columnId).orElseThrow();
    task.setColumn(column);
    task.setPosition(column.getTasks().size());
    Task saved = taskRepository.save(task);
    column.getTasks().add(saved);
    columnRepository.save(column);
    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(column.getBoard().getId());

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(column.getBoard().getMembers());
    members.add(column.getBoard().getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }

    return saved;
  }

  @Transactional
  public Task moveTask(Long taskId, Long targetColumnId, int position) {
    Task task = taskRepository.findById(taskId).orElseThrow();
    BoardColumn old = task.getColumn();
    BoardColumn target = columnRepository.findById(targetColumnId).orElseThrow();

    // 1. Task der neuen Spalte zuweisen
    task.setColumn(target);
    task.setPosition(position);
    Task saved = taskRepository.save(task);

    // 2. Alte Spalte aktualisieren (ohne orphanRemoval auszulösen)
    if (old != null && old.getTasks() != null) {
      old.getTasks().removeIf(t -> t.getId().equals(task.getId()));
    }

    // 3. Neue Spalte aktualisieren
    if (target.getTasks() == null) {
      target.setTasks(new ArrayList<>());
    }
    target.getTasks().add(saved);

    // 4. Positionen neu sortieren
    for (int i = 0; i < target.getTasks().size(); i++) {
      target.getTasks().get(i).setPosition(i);
    }

    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(target.getBoard().getId());

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(target.getBoard().getMembers());
    members.add(target.getBoard().getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }

    return saved;
  }

  @Transactional
  public Board updateBoard(Long boardId, JsonNode json) {
    Board board = boardRepository.findById(boardId).orElseThrow();

    // Titel aktualisieren, wenn vorhanden
    if (json.has("title")) {
      String title = json.get("title").asString();
      board.setTitle(title);
    }

    // Hintergrund aktualisieren, wenn vorhanden
    if (json.has("background")) {
      JsonNode bgNode = json.get("background");

      if (bgNode.isNull() || bgNode.asString().trim().isEmpty()) {
        board.setBackground(null); // löschen
      } else {
        board.setBackground(bgNode.asString()); // setzen
      }
    }

    Board saved = boardRepository.save(board);

    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(saved.getId());

    // 2. User-Update für alle Mitglieder, damit Dropdown aktualisiert wird
    Set<User> members = new HashSet<>(saved.getMembers());
    members.add(saved.getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }

    return saved;
  }

  @Transactional
  public Task updateTask(Long taskId, TaskUpdateRequest req) {
    Task task =
        taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));

    // Vorherige Assignees merken
    Set<User> previousAssignees = new HashSet<>(task.getAssignees());

    if (req.title != null) task.setTitle(req.title);
    if (req.description != null) task.setDescription(req.description);
    if (req.labels != null) task.setLabels(req.labels);
    if (req.attachments != null) task.setAttachments(req.attachments);

    if (req.deadline != null) {
      task.setDeadline(LocalDateTime.parse(req.deadline));
    } else {
      task.setDeadline(null);
    }

    // Neue Assignees übernehmen
    Set<User> newAssignees = new HashSet<>();
    if (req.assigneeIds != null) {
      newAssignees = new HashSet<>(userRepository.findAllById(req.assigneeIds));
      task.setAssignees(newAssignees);
    }

    // Board- und Spalteninfos für E-Mail
    String boardName = task.getColumn().getBoard().getTitle();
    String statusCategory = task.getColumn().getTitle();
    String taskTitle = task.getTitle();

    // 1. E-Mail für NEUE Zuweisungen
    for (User u : newAssignees) {
      if (!previousAssignees.contains(u)) {
        emailService.sendAccountAssignedEmail(u.getEmail(), boardName, statusCategory, taskTitle);
      }
    }

    // 2. E-Mail für ENTFERNTE Zuweisungen
    for (User u : previousAssignees) {
      if (!newAssignees.contains(u)) {
        emailService.sendAccountUnassignedEmail(u.getEmail(), boardName, statusCategory, taskTitle);
      }
    }

    Task saved = taskRepository.save(task);
    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(saved.getColumn().getBoard().getId());

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(saved.getColumn().getBoard().getMembers());
    members.add(saved.getColumn().getBoard().getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }

    return saved;
  }

  @Transactional
  public void deleteTask(Long taskId) {
    Task task = taskRepository.findById(taskId).orElseThrow();
    BoardColumn column = task.getColumn();
    if (column != null) {
      column.getTasks().removeIf(t -> t.getId().equals(taskId));
      columnRepository.save(column);
    }
    taskRepository.deleteById(taskId);
    assert column != null;
    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(column.getBoard().getId());

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(column.getBoard().getMembers());
    members.add(column.getBoard().getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }
  }

  @Transactional(readOnly = true)
  public BoardResponse mapToResponse(Board board) {
    BoardResponse response = new BoardResponse();
    response.setId(board.getId());
    response.setTitle(board.getTitle());
    response.setBackground(board.getBackground());

    // Columns
    response.setColumns(
        board.getColumns().stream()
            .map(
                c -> {
                  BoardColumnResponse cr = new BoardColumnResponse();
                  cr.setId(c.getId());
                  cr.setTitle(c.getTitle());
                  cr.setPosition(c.getPosition());
                  cr.setBoardId(board.getId());
                  List<TaskResponse> taskResponses =
                      c.getTasks().stream()
                          .sorted(Comparator.comparingInt(Task::getPosition))
                          .map(TaskResponse::fromEntity)
                          .collect(Collectors.toList());
                  cr.setTasks(taskResponses);
                  return cr;
                })
            .collect(Collectors.toList()));

    // Owner + Members zusammenführen
    Set<User> allMembers = new LinkedHashSet<>(board.getMembers());
    allMembers.add(board.getOwner());
    response.setMembers(
        allMembers.stream()
            .sorted(Comparator.comparingLong(User::getId)) // nach ID aufsteigend
            .map(
                u ->
                    new UserSummaryResponse(
                        u.getId(), u.getName(), u.getEmail(), u.isEmailChanged(), u.getImage()))
            .collect(Collectors.toList()) // Liste statt Set, Reihenfolge bleibt
        );

    response.setOwner(
        new UserSummaryResponse(
            board.getOwner().getId(),
            board.getOwner().getName(),
            board.getOwner().getEmail(),
            board.getOwner().isEmailChanged(),
            board.getOwner().getImage()));

    return response;
  }

  @Transactional
  public void deleteColumn(Long columnId) {
    BoardColumn column =
        columnRepository
            .findById(columnId)
            .orElseThrow(() -> new RuntimeException("Column not found"));

    // Alle Tasks der Spalte löschen
    taskRepository.deleteAll(column.getTasks());

    // Spalte löschen
    columnRepository.delete(column);
    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(column.getBoard().getId());

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(column.getBoard().getMembers());
    members.add(column.getBoard().getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }
  }

  @Transactional
  public void reorderColumns(Long boardId, List<BoardColumnPositionUpdate> updates) {
    Board board =
        boardRepository
            .findById(boardId)
            .orElseThrow(() -> new RuntimeException("Board not found"));

    Map<Long, Integer> posMap =
        updates.stream().collect(Collectors.toMap(u -> u.id, u -> u.position));

    for (BoardColumn col : board.getColumns()) {
      if (posMap.containsKey(col.getId())) {
        col.setPosition(posMap.get(col.getId()));
      }
    }

    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(boardId);

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(board.getMembers());
    members.add(board.getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }

    boardRepository.save(board);
  }

  @Transactional
  public Board updateColumn(Long columnId, JsonNode json) {
    BoardColumn col = columnRepository.findById(columnId).orElseThrow();

    if (json.has("title")) {
      String title = json.get("title").asString();
      col.setTitle(title);
    }

    // Board zurückgeben, damit mapToResponse() funktioniert
    Board saved = col.getBoard();

    // 1. Board-Update für alle Nutzer, die gerade auf diesem Board sind
    notifyBoard(saved.getId());

    // 2. User-Update für alle Mitglieder eingeladene Mitglieder bei jedem erscheinen
    Set<User> members = new HashSet<>(saved.getMembers());
    members.add(saved.getOwner()); // Owner nicht vergessen

    for (User u : members) {
      notifyUser(u.getId());
    }

    return saved;
  }

  private void notifyBoard(Long boardId) {
    messagingTemplate.convertAndSend("/topic/board/" + boardId, "update");
  }

  private void notifyUser(Long userId) {
    messagingTemplate.convertAndSend("/topic/user/" + userId, "update");
  }
}
