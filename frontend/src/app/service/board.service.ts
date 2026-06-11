/**
 * Diese Service-Klasse bietet Methoden zum Verwalten von Boards, Spalten und Aufgaben.
 * Sie kommuniziert mit einer Backend-API, um Daten zu erstellen, abzurufen, zu aktualisieren und zu löschen.
 * Die Methoden verwenden HTTP-Anfragen, um die entsprechenden Aktionen auf dem Server auszuführen.
 */
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BoardModel } from '../model/board.model';
import { ColumnModel } from '../model/column.model';
import { TaskModel } from '../model/task.model';

@Injectable({ providedIn: 'root' })
export class BoardService {
  private readonly http: HttpClient = inject(HttpClient);
  private readonly API: string = 'http://localhost:8080/api/boards';

  /**
   * Ruft eine Liste aller Boards ab.
   * Diese Methode sendet eine GET-Anfrage an die Backend-API und gibt ein Observable zurück,
   * das ein Array von BoardModel-Objekten enthält.
   *
   * @returns Ein Observable, das ein Array von BoardModel-Objekten enthält.
   */
  listBoards(): Observable<BoardModel[]> {
    return this.http.get<BoardModel[]>(this.API);
  }

  /**
   * Ruft ein bestimmtes Board anhand seiner ID ab.
   * Diese Methode sendet eine GET-Anfrage an die Backend-API mit der Board-ID in der URL und gibt ein Observable zurück,
   * das ein BoardModel-Objekt enthält.
   *
   * @param id Die ID des Boards, das abgerufen werden soll.
   * @returns Ein Observable, das ein BoardModel-Objekt enthält.
   */
  getBoard(id: number): Observable<BoardModel> {
    return this.http.get<BoardModel>(`${this.API}/${id}`);
  }

  /**
   * Erstellt ein neues Board für einen bestimmten Benutzer.
   * Diese Methode sendet eine POST-Anfrage an die Backend-API mit den Board-Daten im Request-Body.
   *
   * @param board Die Daten des Boards, das erstellt werden soll. Es handelt sich um ein Partial-Objekt,
   * da nicht alle Felder erforderlich sind.
   * @returns Ein Observable, das das erstellte BoardModel-Objekt enthält.
   */
  createBoard(board: Partial<BoardModel>): Observable<BoardModel> {
    return this.http.post<BoardModel>(this.API, board);
  }

  /**
   * Aktualisiert ein bestehendes Board anhand seiner ID.
   * Diese Methode sendet eine PUT-Anfrage an die Backend-API mit der Board-ID in der URL und den aktualisierten
   * Board-Daten im Request-Body.
   *
   * @param boardId Die ID des Boards, das aktualisiert werden soll.
   * @param board Die aktualisierten Daten des Boards. Es handelt sich um ein Partial-Objekt,
   * da nicht alle Felder erforderlich sind.
   * @return Ein Observable, das das aktualisierte BoardModel-Objekt enthält.
   */
  updateBoard(boardId: number, board: Partial<BoardModel>): Observable<BoardModel> {
    return this.http.put<BoardModel>(`${this.API}/${boardId}`, board);
  }

  /**
   * Löscht ein Board anhand seiner ID.
   * Diese Methode sendet eine DELETE-Anfrage an die Backend-API mit der Board-ID in der URL und der E-Mail-Adresse
   * des Benutzers im Request-Body.
   *
   * @param boardId Die ID des Boards, das gelöscht werden soll.
   * @param email Die E-Mail-Adresse des Benutzers, der das Board löschen möchte.
   * @return Ein Observable, das ein leeres Objekt enthält, nachdem das Board gelöscht wurde.
   */
  invite(boardId: number, email: string): Observable<BoardModel> {
    return this.http.post<BoardModel>(`${this.API}/${boardId}/invite`, { email });
  }

  /**
   * Fügt eine neue Spalte zu einem Board hinzu, indem die Board-ID und der Spaltentitel übergeben werden.
   * Diese Methode sendet eine POST-Anfrage an die Backend-API mit der Board-ID in der URL und dem Spaltentitel im
   * Request-Body.
   *
   * @param boardId Die ID des Boards, zu dem die Spalte hinzugefügt werden soll.
   * @param title Der Titel der Spalte, die hinzugefügt werden soll.
   * @returns Ein Observable, das das erstellte ColumnModel-Objekt enthält, nachdem die Spalte hinzugefügt wurde.
   */
  addColumn(boardId: number, title: string): Observable<ColumnModel> {
    return this.http.post<ColumnModel>(`${this.API}/${boardId}/columns`, { title });
  }

  /**
   * Aktualisiert eine bestehende Spalte anhand ihrer ID.
   * Diese Methode sendet eine PUT-Anfrage an die Backend-API mit der Spalten-ID in der URL und den aktualisierten
   * Spalten-Daten im Request-Body.
   *
   * @param columnId Die ID der Spalte, die aktualisiert werden soll.
   * @param column Die aktualisierten Daten der Spalte. Es handelt sich um ein Partial-Objekt,
   * da nicht alle Felder erforderlich sind.
   * @returns Ein Observable, das das aktualisierte BoardModel-Objekt enthält.
   */
  updateColumn(columnId: number, column: Partial<ColumnModel>): Observable<BoardModel> {
    return this.http.put<BoardModel>(`${this.API}/columns/${columnId}`, column);
  }

  /**
   * Verschiebt eine Spalte an eine neue Position innerhalb eines Boards.
   * Diese Methode sendet eine PUT-Anfrage an die Backend-API mit der Spalten-ID in der URL und der neuen Position
   * als Query-Parameter.
   *
   * @param columnId Die ID der Spalte, die verschoben werden soll.
   * @param position Die neue Position der Spalte innerhalb des Boards (0-basiert).
   * @returns Ein Observable, das ein leeres Objekt enthält, nachdem die Spalte verschoben wurde.
   */
  moveColumn(columnId: number, position: number): Observable<void> {
    return this.http.put<void>(`${this.API}/columns/${columnId}/move?position=${position}`, {});
  }

  /**
   * Löscht eine Spalte anhand ihrer ID.
   * Diese Methode sendet eine DELETE-Anfrage an die Backend-API mit der Spalten-ID in der URL.
   *
   * @param columnId Die ID der Spalte, die gelöscht werden soll.
   * @returns Ein Observable, das ein leeres Objekt enthält, nachdem die Spalte gelöscht wurde.
   */
  deleteColumn(columnId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/columns/${columnId}`);
  }

  /**
   * Fügt eine neue Aufgabe zu einer Spalte hinzu, indem die Spalten-ID und die Aufgabendaten übergeben werden.
   * Diese Methode sendet eine POST-Anfrage an die Backend-API mit der Spalten-ID in der URL und den Aufgabendaten
   * im Request-Body.
   *
   * @param columnId Die ID der Spalte, zu der die Aufgabe hinzugefügt werden soll.
   * @param task Die Daten der Aufgabe, die hinzugefügt werden soll. Es handelt sich um ein Partial-Objekt,
   * da nicht alle Felder erforderlich sind.
   * @returns Ein Observable, das das erstellte TaskModel-Objekt enthält, nachdem die Aufgabe hinzugefügt wurde.
   */
  addTask(columnId: number, task: Partial<TaskModel>): Observable<TaskModel> {
    return this.http.post<TaskModel>(`${this.API}/columns/${columnId}/tasks`, task);
  }

  /**
   * Aktualisiert eine bestehende Aufgabe anhand ihrer ID.
   * Diese Methode sendet eine PUT-Anfrage an die Backend-API mit der Aufgaben-ID in der URL und den aktualisierten
   * Aufgabendaten im Request-Body.
   *
   * @param taskId Die ID der Aufgabe, die aktualisiert werden soll.
   * @param partial Die aktualisierten Daten der Aufgabe. Es handelt sich um ein Partial-Objekt,
   * da nicht alle Felder erforderlich sind.
   * @returns Ein Observable, das das aktualisierte TaskModel-Objekt enthält.
   */
  updateTask(taskId: number, partial: Partial<TaskModel>): Observable<TaskModel> {
    return this.http.put<TaskModel>(`${this.API}/tasks/${taskId}`, partial);
  }

  /**
   * Verschiebt eine Aufgabe an eine neue Position innerhalb einer Spalte oder zu einer anderen Spalte.
   * Diese Methode sendet eine PUT-Anfrage an die Backend-API mit der Aufgaben-ID in der URL und der Ziel-Spalten-ID
   * sowie der neuen Position als Query-Parameter.
   *
   * @param taskId Die ID der Aufgabe, die verschoben werden soll.
   * @param targetColumnId Die ID der Spalte, zu der die Aufgabe verschoben werden soll.
   * @param position Die neue Position der Aufgabe innerhalb der Ziel-Spalte (0-basiert).
   * @returns Ein Observable, das das aktualisierte TaskModel-Objekt enthält, nachdem die Aufgabe verschoben wurde.
   */
  moveTask(taskId: number, targetColumnId: number, position: number): Observable<TaskModel> {
    return this.http.put<TaskModel>(
      `${this.API}/tasks/${taskId}/move?targetColumnId=${targetColumnId}&position=${position}`,
      {}
    );
  }

  /**
   * Löscht eine Aufgabe anhand ihrer ID.
   * Diese Methode sendet eine DELETE-Anfrage an die Backend-API mit der Aufgaben-ID in der URL.
   *
   * @param taskId Die ID der Aufgabe, die gelöscht werden soll.
   * @returns Ein Observable, das ein leeres Objekt enthält, nachdem die Aufgabe gelöscht wurde.
   */
  deleteTask(taskId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/tasks/${taskId}`);
  }
}
