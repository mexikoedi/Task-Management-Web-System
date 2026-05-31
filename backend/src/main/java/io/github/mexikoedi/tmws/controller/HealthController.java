/**
 * Diese Klasse ist ein REST-Controller, der den Gesundheitsstatus der Anwendung bereitstellt.
 */
package io.github.mexikoedi.tmws.controller;

import io.github.mexikoedi.tmws.dto.HealthResponse;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {
  private final BuildProperties buildProperties;

  /**
   * Konstruktor, der die BuildProperties injiziert, um Informationen über die Anwendung bereitzustellen.
   *
   * @param buildProperties Die BuildProperties, die Informationen über die Anwendung enthalten,
   * wie Version, Name und Build-Zeit.
   * Diese werden aus der build-info.properties-Datei geladen, die während des Build-Prozesses generiert wird.
   */
  public HealthController(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  /**
   * Diese Methode behandelt GET-Anfragen an den Endpunkt "/api/health" und gibt den Gesundheitsstatus der
   * Anwendung zurück.
   *
   * @return Ein ResponseEntity, das ein HealthResponse-Objekt enthält, das den Status "UP", eine Nachricht,
   * die Version, den Namen und die Build-Zeit der Anwendung enthält.
   * Das HealthResponse-Objekt wird als JSON zurückgegeben, wenn die Anfrage erfolgreich ist.
   */
  @GetMapping
  public ResponseEntity<HealthResponse> health() {
    return ResponseEntity.ok(
      new HealthResponse(
        "UP",
        "TMWS Backend is running",
        buildProperties.getVersion(),
        buildProperties.getName(),
        buildProperties.getTime()
      )
    );
  }
}
