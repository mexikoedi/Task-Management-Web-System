/**
 * Diese Klasse enthält Tests für die Anwendung. Sie überprüft, ob der Anwendungskontext erfolgreich
 * geladen wird. Es ist eine grundlegende Testklasse, die sicherstellt, dass die Spring
 * Boot-Anwendung korrekt konfiguriert ist und keine Fehler beim Starten auftreten. Weitere
 * spezifische Tests können in dieser Klasse oder in separaten Testklassen hinzugefügt werden, um
 * die Funktionalität der Anwendung zu überprüfen.
 */
package io.github.mexikoedi.tmws;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TmwsApplicationTests {
  @Test
  void contextLoads() {}
}
