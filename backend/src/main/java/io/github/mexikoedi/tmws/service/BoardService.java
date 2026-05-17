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

  public BoardService(
    BoardRepository boardRepository,
    BoardColumnRepository columnRepository,
    TaskRepository taskRepository,
    UserRepository userRepository, EmailService emailService) {
    this.boardRepository = boardRepository;
    this.columnRepository = columnRepository;
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.emailService = emailService;
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

    return boardRepository.save(board);
  }

  @Transactional
  public Board inviteMember(Long boardId, String email) {

    Board board = boardRepository.findById(boardId)
      .orElseThrow(() -> new ResourceNotFoundException("Board nicht gefunden"));

    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new ResourceNotFoundException("Benutzer existiert nicht"));

    if (board.getMembers().contains(user)) {
      throw new EmailAlreadyExistsException("Benutzer ist bereits Mitglied");
    }

    board.getMembers().add(user);

    emailService.sendAccountInvitedEmail(user.getEmail(), board.getTitle());

    return boardRepository.save(board);
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

    return boardRepository.save(board);
  }

  @Transactional
  public Task updateTask(Long taskId, TaskUpdateRequest req) {
    Task task = taskRepository.findById(taskId)
      .orElseThrow(() -> new RuntimeException("Task not found"));

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
        emailService.sendAccountAssignedEmail(
          u.getEmail(),
          boardName,
          statusCategory,
          taskTitle
        );
      }
    }

    // 2. E-Mail für ENTFERNTE Zuweisungen
    for (User u : previousAssignees) {
      if (!newAssignees.contains(u)) {
        emailService.sendAccountUnassignedEmail(
          u.getEmail(),
          boardName,
          statusCategory,
          taskTitle
        );
      }
    }

    return taskRepository.save(task);
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
  }

  @Transactional(readOnly = true)
  public BoardResponse mapToResponse(Board board) {
    BoardResponse response = new BoardResponse();
    response.setId(board.getId());
    response.setTitle(board.getTitle());
    response.setBackground(board.getBackground());

    // Columns
    response.setColumns(board.getColumns().stream().map(c -> {
      BoardColumnResponse cr = new BoardColumnResponse();
      cr.setId(c.getId());
      cr.setTitle(c.getTitle());
      cr.setPosition(c.getPosition());
      cr.setBoardId(board.getId());
      List<TaskResponse> taskResponses = c.getTasks().stream()
        .sorted(Comparator.comparingInt(Task::getPosition))
        .map(TaskResponse::fromEntity)
        .collect(Collectors.toList());
      cr.setTasks(taskResponses);
      return cr;
    }).collect(Collectors.toList()));

    // Members
    response.setMembers(
      board.getMembers().stream()
        .sorted(Comparator.comparingLong(User::getId)) // nach ID aufsteigend
        .map(u -> new UserSummaryResponse(u.getId(), u.getName(), u.getEmail(), u.isEmailChanged(), u.getImage()))
        .collect(Collectors.toList()) // Liste statt Set, Reihenfolge bleibt
    );

    return response;
  }

  @Transactional
  public void deleteColumn(Long columnId) {
    BoardColumn column = columnRepository.findById(columnId)
      .orElseThrow(() -> new RuntimeException("Column not found"));

    // Alle Tasks der Spalte löschen
    taskRepository.deleteAll(column.getTasks());

    // Spalte löschen
    columnRepository.delete(column);
  }

  @Transactional
  public void reorderColumns(Long boardId, List<BoardColumnPositionUpdate> updates) {
    Board board = boardRepository.findById(boardId)
      .orElseThrow(() -> new RuntimeException("Board not found"));

    Map<Long, Integer> posMap = updates.stream()
      .collect(Collectors.toMap(u -> u.id, u -> u.position));

    for (BoardColumn col : board.getColumns()) {
      if (posMap.containsKey(col.getId())) {
        col.setPosition(posMap.get(col.getId()));
      }
    }

    boardRepository.save(board);
  }

  @Transactional
  public Board updateColumn(Long columnId, JsonNode json) {
    BoardColumn col = columnRepository.findById(columnId)
      .orElseThrow();

    if (json.has("title")) {
      String title = json.get("title").asString();
      col.setTitle(title);
    }

    // Board zurückgeben, damit mapToResponse() funktioniert
    return col.getBoard();
  }
}

