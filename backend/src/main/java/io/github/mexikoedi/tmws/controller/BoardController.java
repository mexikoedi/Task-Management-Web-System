package io.github.mexikoedi.tmws.controller;

import io.github.mexikoedi.tmws.dto.*;
import io.github.mexikoedi.tmws.model.Board;
import io.github.mexikoedi.tmws.model.BoardColumn;
import io.github.mexikoedi.tmws.model.Task;
import io.github.mexikoedi.tmws.service.BoardService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
  private final BoardService boardService;

  public BoardController(BoardService boardService) {
    this.boardService = boardService;
  }

  @GetMapping
  public ResponseEntity<List<BoardResponse>> listBoards() {
    List<BoardResponse> boards =
        boardService.findAll().stream()
            .map(boardService::mapToResponse)
            .collect(Collectors.toList());
    return ResponseEntity.ok(boards);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BoardResponse> getBoard(@PathVariable Long id) {
    return boardService
        .findById(id)
        .map(boardService::mapToResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<BoardResponse> createBoard(
      @RequestBody Board board, @RequestParam String ownerEmail) {
    Board created = boardService.createBoard(board, ownerEmail);
    return ResponseEntity.ok(boardService.mapToResponse(created));
  }

  @PostMapping("/{id}/invite")
  public ResponseEntity<BoardResponse> invite(@PathVariable Long id, @RequestParam String email) {
    Board updated = boardService.inviteMember(id, email);
    return ResponseEntity.ok(boardService.mapToResponse(updated));
  }

  @PostMapping("/{id}/columns")
  public ResponseEntity<BoardColumnResponse> addColumn(
      @PathVariable Long id, @RequestParam String title) {
    BoardColumn column = boardService.addColumn(id, title);
    BoardColumnResponse response = new BoardColumnResponse();
    response.setId(column.getId());
    response.setTitle(column.getTitle());
    response.setPosition(column.getPosition());
    response.setBoardId(column.getBoard().getId());
    List<TaskResponse> taskResponses =
        column.getTasks().stream().map(TaskResponse::fromEntity).collect(Collectors.toList());
    response.setTasks(taskResponses);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/columns/{columnId}/tasks")
  public ResponseEntity<Task> addTask(@PathVariable Long columnId, @RequestBody Task task) {
    Task saved = boardService.addTask(columnId, task);
    return ResponseEntity.ok(saved);
  }

  @PutMapping("/tasks/{taskId}/move")
  public ResponseEntity<Task> moveTask(
      @PathVariable Long taskId, @RequestParam Long targetColumnId, @RequestParam int position) {
    Task moved = boardService.moveTask(taskId, targetColumnId, position);
    return ResponseEntity.ok(moved);
  }

  @PutMapping("/{id}")
  public ResponseEntity<BoardResponse> updateBoard(
      @PathVariable Long id, @RequestBody JsonNode json) {

    Board updated = boardService.updateBoard(id, json);
    return ResponseEntity.ok(boardService.mapToResponse(updated));
  }

  @PutMapping("/tasks/{taskId}")
  public ResponseEntity<TaskResponse> updateTask(
      @PathVariable Long taskId, @RequestBody TaskUpdateRequest request) {

    Task updated = boardService.updateTask(taskId, request);
    return ResponseEntity.ok(TaskResponse.fromEntity(updated));
  }

  @DeleteMapping("/tasks/{taskId}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
    boardService.deleteTask(taskId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/columns/{columnId}")
  public ResponseEntity<Void> deleteColumn(@PathVariable Long columnId) {
    boardService.deleteColumn(columnId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{boardId}/columns/reorder")
  public void reorderColumns(
      @PathVariable Long boardId, @RequestBody List<BoardColumnPositionUpdate> updates) {
    boardService.reorderColumns(boardId, updates);
  }

  @PutMapping("/columns/{id}")
  public ResponseEntity<BoardResponse> updateColumn(
      @PathVariable Long id, @RequestBody JsonNode json) {

    Board updatedBoard = boardService.updateColumn(id, json);
    return ResponseEntity.ok(boardService.mapToResponse(updatedBoard));
  }
}
