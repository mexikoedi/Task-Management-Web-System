/**
 * Diese Klasse konfiguriert die Cross-Origin Resource Sharing (CORS) Einstellungen für die
 * Anwendung.
 */
package io.github.mexikoedi.tmws.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  /**
   * Diese Methode definiert die CORS-Regeln für die Anwendung. Sie erlaubt Anfragen von <a
   * href="http://localhost:4200">http://localhost:4200</a> an die Endpunkte unter /api/**, erlaubt
   * die HTTP-Methoden GET, POST, PUT und DELETE, sowie die Header Content-Type und Authorization.
   * Die maxAge gibt an, wie lange die CORS-Preflight-Anfrage gecached werden kann (in Sekunden).
   *
   * @param registry Der CorsRegistry, der die CORS-Regeln konfiguriert.
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins("http://localhost:4200")
        .allowedMethods("GET", "POST", "PUT", "DELETE")
        .allowedHeaders("Content-Type", "Authorization")
        .maxAge(3600);
  }
}
