/**
 * Diese Klasse repräsentiert die Antwort auf eine Gesundheitsprüfungsanfrage (Health Check) an die Anwendung.
 */
package io.github.mexikoedi.tmws.dto;

import lombok.*;
import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
public class HealthResponse {
  private String status;
  private String message;
  private String version;
  private String name;
  private Instant time;
}
