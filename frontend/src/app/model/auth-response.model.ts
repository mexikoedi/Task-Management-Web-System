/**
 * Das Interface `LoginResponse` definiert die Struktur der Antwort, die vom Server nach einem erfolgreichen Login
 * zurückgegeben wird. Es enthält ein `token`-Feld, das den Authentifizierungs-Token enthält, der für zukünftige Anfragen
 * verwendet werden kann. Das `RegisterResponse`-Interface definiert die Struktur der Antwort nach einer erfolgreichen
 * Registrierung, einschließlich der `id` und `email` des neu registrierten Benutzers.
 */
export interface LoginResponse {
  token: string;
}

export interface RegisterResponse {
  id: number;
  email: string;
}
