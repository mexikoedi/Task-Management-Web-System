/**
 * Das UserModel repräsentiert die Datenstruktur eines Benutzers in der Anwendung.
 */
export interface UserModel {
  id: number;
  name: string;
  email: string;
  image?: string | null;
  emailVerified: boolean;
  emailChanged: boolean;
}
