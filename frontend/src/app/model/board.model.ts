export interface UserSummary {
  id?: number | null;
  name: string;
  email: string;
  emailChanged: boolean;
  image?: string;
}

export interface TodoItem {
  id?: number;
  text: string;
  completed: boolean;
}

export interface TaskModel {
  id?: number;
  title: string;
  description?: string;
  deadline?: string | null;
  position?: number;
  labels?: string; // comma-separated
  attachments?: string; // comma-separated
  todos?: TodoItem[];
  assignees?: UserSummary[];
}

export interface ColumnModel {
  id: number;
  title: string;
  position?: number;
  tasks?: TaskModel[];
}

export interface BoardModel {
  id?: number;
  owner?: UserSummary;
  title: string;
  background?: string | null;
  columns?: ColumnModel[];
  members?: UserSummary[];
}

export interface UpdateProfileInfo {
  name?: string;
}

export interface ChangePasswordInfo {
  currentPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
}
