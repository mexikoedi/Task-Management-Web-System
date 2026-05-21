import { Injectable, signal } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';

@Injectable({ providedIn: 'root' })
export class WebsocketService {
  boardUpdates = signal(0);
  userUpdates = signal(0);
  private client: Client | null = null;
  private boardSub: StompSubscription | null = null;
  private userSub: StompSubscription | null = null;

  private ensureClient() {
    if (this.client) return;

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 500,
      debug: () => {},
    });

    this.client.activate();
  }

  private onConnected(fn: () => void) {
    if (!this.client) return;

    if (this.client.connected) {
      fn();
    } else {
      const prev = this.client.onConnect;
      this.client.onConnect = frame => {
        prev?.(frame);
        fn();
      };
    }
  }

  subscribeBoard(boardId: number) {
    this.ensureClient();

    this.onConnected(() => {
      this.boardSub?.unsubscribe();
      this.boardSub = this.client!.subscribe(`/topic/board/${boardId}`, () => {
        this.boardUpdates.update(v => v + 1);
      });
    });
  }

  subscribeUser(userId: number) {
    this.ensureClient();

    this.onConnected(() => {
      this.userSub?.unsubscribe();
      this.userSub = this.client!.subscribe(`/topic/user/${userId}`, () => {
        this.userUpdates.update(v => v + 1);
      });
    });
  }

  disconnectBoard() {
    this.boardSub?.unsubscribe();
    this.boardSub = null;
  }

  disconnectAll() {
    this.boardSub?.unsubscribe();
    this.userSub?.unsubscribe();
    this.boardSub = null;
    this.userSub = null;
    this.client?.deactivate();
    this.client = null;
  }
}
