/**
 * Das ColumnModel repräsentiert eine Statuskategorie in dem Projektboard.
 */
import { TaskModel } from './task.model';

export interface ColumnModel {
  id: number;
  title: string;
  position: number;
  boardId: number;
  tasks?: TaskModel[] | null;
}
