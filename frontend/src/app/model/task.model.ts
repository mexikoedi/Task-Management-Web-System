/**
 * Das TaskModel repräsentiert eine Aufgabe in der Anwendung.
 */
import { UserModel } from './user.model';

export interface TaskModel {
  id?: number | undefined;
  title?: string | null;
  description?: string | null;
  deadline?: string | null;
  position?: number | null;
  labels?: string | null;
  attachments?: string | null;
  assignees?: UserModel[] | null;
}
