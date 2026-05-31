/**
 * Diese Klasse repräsentiert die Struktur der API-Antwort, die an den Client zurückgegeben wird.
 */
package io.github.mexikoedi.tmws.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
public class ApiResponse {
  private String token;
  private String message;
  private boolean success;
}
