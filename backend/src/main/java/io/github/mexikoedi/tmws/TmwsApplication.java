/** Diese Klasse ist der Einstiegspunkt für die Spring Boot Anwendung. */
package io.github.mexikoedi.tmws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TmwsApplication {
  /**
   * Die main-Methode startet die Spring Boot Anwendung.
   *
   * @param args die Kommandozeilenargumente, die an die Anwendung übergeben werden können.
   */
  static void main(String[] args) {
    SpringApplication.run(TmwsApplication.class, args);
  }
}
