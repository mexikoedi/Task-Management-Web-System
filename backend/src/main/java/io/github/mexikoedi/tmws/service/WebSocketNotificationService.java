/**
 * Diese Klasse ist für die Benachrichtigung von Clients über WebSockets zuständig.
 */
package io.github.mexikoedi.tmws.service;

import io.github.mexikoedi.tmws.model.Board;
import io.github.mexikoedi.tmws.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class WebSocketNotificationService {
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Konstruktor für die WebSocketNotificationService-Klasse.
   *
   * @param messagingTemplate Das SimpMessagingTemplate, das für die Kommunikation mit den WebSocket-Clients
   * verwendet wird.
   */
  public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Diese Methode sendet eine Nachricht an alle Clients, die mit dem angegebenen Benutzer verbunden sind,
   * um sie abzumelden.
   *
   * @param userId Die ID des Benutzers, der abgemeldet werden soll.
   * @param newTokenVersion Die neue Token-Version, die in der Nachricht enthalten sein soll, damit die Clients
   * wissen, dass sie ihre Tokens ungültig machen müssen.
   */
  public void sendForceLogout(Long userId, int newTokenVersion) {
    String payload = """
        {"type":"force_logout","tokenVersion":%d}
        """.formatted(newTokenVersion);
    messagingTemplate.convertAndSend("/topic/user/" + userId, payload);
  }

  /**
   * Diese Methode sendet eine Nachricht an alle Clients, die mit dem angegebenen Board verbunden sind, um sie zu
   * benachrichtigen, dass das Board aktualisiert wurde. Zusätzlich werden alle Mitglieder des Boards benachrichtigt,
   * damit sie die Änderungen sehen können.
   *
   * @param board Das Board, das aktualisiert wurde und dessen Mitglieder benachrichtigt werden sollen.
   */
  public void notifyBoardAndMembers(Board board) {
    Set<Long> userIds = new HashSet<>();
    userIds.add(board.getOwner().getId());
    for (User u : board.getMembers()) userIds.add(u.getId());
    for (Long id : userIds) notifyUser(id);
    notifyBoard(board.getId());
  }

  /**
   * Diese Methode sendet eine Nachricht an alle Clients, die mit dem angegebenen Benutzer verbunden sind,
   * um sie zu benachrichtigen, dass sich etwas geändert hat.
   *
   * @param userId Die ID des Benutzers, dessen Clients benachrichtigt werden sollen.
   */
  private void notifyUser(Long userId) {
    messagingTemplate.convertAndSend("/topic/user/" + userId, "update");
  }

  /**
   * Diese Methode sendet eine Nachricht an alle Clients, die mit dem angegebenen Board verbunden sind, um sie zu
   * benachrichtigen, dass sich etwas geändert hat.
   *
   * @param boardId Die ID des Boards, dessen Clients benachrichtigt werden sollen.
   */
  private void notifyBoard(Long boardId) {
    messagingTemplate.convertAndSend("/topic/board/" + boardId, "update");
  }
}
