/**
 * Diese Klasse repräsentiert die Struktur einer Fehlerantwort, die von der API zurückgegeben wird.
 */
package io.github.mexikoedi.tmws.service.dto;

import java.time.LocalDateTime;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
public class ErrorResponse {
  private LocalDateTime timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
}
