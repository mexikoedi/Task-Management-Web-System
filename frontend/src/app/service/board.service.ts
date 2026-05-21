import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BoardModel, ColumnModel, TaskModel } from '../model/board.model';

@Injectable({ providedIn: 'root' })
export class BoardService {
  private readonly API = 'http://localhost:8080/api/boards';

  constructor(private http: HttpClient) {}

  list(): Observable<BoardModel[]> {
    return this.http.get<BoardModel[]>(this.API);
  }

  get(id: number) {
    return this.http.get<BoardModel>(`${this.API}/${id}`);
  }

  create(board: Partial<BoardModel>, ownerEmail: string) {
    return this.http.post<BoardModel>(`${this.API}?ownerEmail=${encodeURIComponent(ownerEmail)}`, board);
  }

  invite(boardId: number, email: string) {
    return this.http.post<BoardModel>(`${this.API}/${boardId}/invite?email=${encodeURIComponent(email)}`, {});
  }

  addColumn(boardId: number, title: string) {
    return this.http.post<ColumnModel>(`${this.API}/${boardId}/columns?title=${encodeURIComponent(title)}`, {});
  }

  addTask(columnId: number, task: Partial<TaskModel>) {
    return this.http.post<TaskModel>(`${this.API}/columns/${columnId}/tasks`, task);
  }

  moveTask(taskId: number, targetColumnId: number, position: number) {
    return this.http.put(`${this.API}/tasks/${taskId}/move?targetColumnId=${targetColumnId}&position=${position}`, {});
  }

  update(boardId: number, partial: Partial<BoardModel>) {
    return this.http.put<BoardModel>(`${this.API}/${boardId}`, partial);
  }

  updateTask(taskId: number, payload: any) {
    return this.http.put<TaskModel>(`${this.API}/tasks/${taskId}`, payload);
  }

  deleteTask(taskId: number) {
    return this.http.delete(`${this.API}/tasks/${taskId}`);
  }

  deleteColumn(columnId: number) {
    return this.http.delete(`${this.API}/columns/${columnId}`);
  }

  updateColumnOrder(columnId: number, payload: any) {
    return this.http.put<ColumnModel[]>(`${this.API}/${columnId}/columns/reorder`, payload);
  }

  updateColumn(columnId: number, partial: Partial<ColumnModel>) {
    return this.http.put<ColumnModel>(`${this.API}/columns/${columnId}`, partial);
  }
}
