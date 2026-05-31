/**
 * Das BoardModel repräsentiert ein Projektboard, das von einem Benutzer erstellt wurde.
 */
import { UserModel } from './user.model';
import { ColumnModel } from './column.model';

export interface BoardModel {
  id: number;
  owner: UserModel;
  title: string;
  background?: string | null;
  columns: ColumnModel[];
  members: UserModel[];
}
