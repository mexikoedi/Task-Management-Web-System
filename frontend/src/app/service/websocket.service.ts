/**
 * Diese Klasse bietet Funktionen zur Verwaltung von WebSocket-Verbindungen und -Abonnements für Board- und User-Updates.
 */
import { Injectable, inject, signal, WritableSignal } from '@angular/core';
import { Client, IFrame, IMessage } from '@stomp/stompjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class WebsocketService {
  boardUpdates: WritableSignal<number> = signal(0);
  userUpdates: WritableSignal<number> = signal(0);
  private client: Client | null = null;
  private pendingBoardId: number | null = null;
  private pendingUserId: number | null = null;
  private readonly auth: AuthService = inject(AuthService);
  private readonly API: string = 'ws://localhost:8080/api/ws';

  /**
   * Initialisiert die WebSocket-Verbindung, um sicherzustellen, dass sie bereit ist, sobald die Anwendung gestartet wird.
   */
  constructor() {
    this.ensureClient();
  }

  /**
   * Abonniert Updates für ein bestimmtes Board, indem es die WebSocket-Verbindung herstellt und die entsprechenden
   * Abonnements einrichtet. Wenn die Verbindung bereits besteht, wird das Abonnement sofort eingerichtet.
   * Andernfalls wird es nach der Verbindung automatisch wiederhergestellt.
   *
   * @param boardId Die ID des Boards, für das Updates abonniert werden sollen.
   */
  subscribeBoard(boardId: number): void {
    this.pendingBoardId = boardId;
    this.ensureClient();

    if (this.client?.connected) {
      this.subscribeBoardInternal(boardId);
    }
  }

  /**
   * Abonniert Updates für einen bestimmten User, indem es die WebSocket-Verbindung herstellt und die entsprechenden
   * Abonnements einrichtet. Wenn die Verbindung bereits besteht, wird das Abonnement sofort eingerichtet.
   * Andernfalls wird es nach der Verbindung automatisch wiederhergestellt.
   *
   * @param userId Die ID des Benutzers, für den Updates abonniert werden sollen.
   */
  subscribeUser(userId: number): void {
    this.pendingUserId = userId;
    this.ensureClient();

    if (this.client?.connected) {
      this.subscribeUserInternal(userId);
    }
  }

  /**
   * Richtet das Abonnement für Board-Updates ein, indem es die WebSocket-Verbindung nutzt, um auf Nachrichten zu hören,
   * die für das angegebene Board gesendet werden. Bei jeder empfangenen Nachricht wird der Zähler für Board-Updates erhöht,
   * um die Anwendung über Änderungen zu informieren.
   *
   * @param boardId Die ID des Boards, für das Updates abonniert werden sollen.
   */
  private subscribeBoardInternal(boardId: number): void {
    if (!this.client?.connected) return;

    this.client.subscribe(`/topic/board/${boardId}`, (): void => {
      this.boardUpdates.update((v: number): number => v + 1);
    });
  }

  /**
   * Richtet das Abonnement für User-Updates ein, indem es die WebSocket-Verbindung nutzt, um auf Nachrichten zu hören,
   * die für den angegebenen Benutzer gesendet werden. Bei jeder empfangenen Nachricht wird der Inhalt überprüft, um zu
   * bestimmen, ob es sich um ein Update oder eine Anweisung zum Erzwingen eines Logouts handelt. Je nach Nachrichtentyp
   * wird entweder der Zähler für User-Updates erhöht oder die Authentifizierungsinformationen gelöscht, um den Benutzer
   * abzumelden.
   *
   * @param userId Die ID des Benutzers, für den Updates abonniert werden sollen.
   */
  private subscribeUserInternal(userId: number): void {
    if (!this.client?.connected) return;

    this.client.subscribe(`/topic/user/${userId}`, (msg: IMessage): void => {
      const raw: string = msg.body?.trim();
      if (!raw) return;

      if (raw === 'update') {
        this.userUpdates.update((v: number): number => v + 1);

        return;
      }

      if (!raw.startsWith('{') && !raw.startsWith('[')) return;

      let body;

      try {
        body = JSON.parse(raw);
      } catch {
        return;
      }

      if (body.type === 'force_logout') {
        this.auth.logout();
      }
    });
  }

  /**
   * Stellt sicher, dass die WebSocket-Verbindung (STOMP-Client) initialisiert und aktiviert ist. Wenn der Client bereits
   * existiert, wird die Methode einfach zurückgegeben. Andernfalls wird ein neuer Client erstellt, konfiguriert und
   * aktiviert, um die Verbindung zum WebSocket-Server herzustellen.
   */
  private ensureClient(): void {
    if (this.client) return;

    this.client = new Client({
      webSocketFactory: (): WebSocket => new WebSocket(this.API),

      beforeConnect: (): void => {
        const token: string | null = sessionStorage.getItem('token');
        this.client!.connectHeaders = token ? { Authorization: 'Bearer ' + token } : {};
      },
      reconnectDelay: 1000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    this.client.onConnect = (): void => {
      this.restoreSubscriptions();
    };

    this.client.onStompError = (frame: IFrame): void => {
      const msg: string | undefined = frame.headers['message'];

      if (msg === 'Session closed.') {
        return;
      }
    };

    this.client.activate();
  }

  /**
   * Stellt sicher, dass alle ausstehenden Abonnements für Board- und User-Updates wiederhergestellt werden, sobald die
   * WebSocket-Verbindung erfolgreich hergestellt wurde. Diese Methode wird aufgerufen, wenn die Verbindung zum
   * WebSocket-Server hergestellt oder wiederhergestellt wird, um sicherzustellen, dass die Anwendung weiterhin über
   * Änderungen informiert wird, auch wenn die Verbindung vorübergehend unterbrochen wurde. Es überprüft, ob es
   * ausstehende Abonnements für Board- und User-Updates gibt und richtet sie entsprechend ein, wenn die Verbindung
   * aktiv ist.
   */
  private restoreSubscriptions(): void {
    if (!this.client?.connected) return;

    if (this.pendingBoardId !== null) {
      this.subscribeBoardInternal(this.pendingBoardId);
    }

    if (this.pendingUserId !== null) {
      this.subscribeUserInternal(this.pendingUserId);
    }
  }
}
