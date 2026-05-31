/**
 * Diese Klasse konfiguriert den asynchronen Task-Executor für die Anwendung.
 */
package io.github.mexikoedi.tmws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
  /**
   * Konfiguriert den Task-Executor für asynchrone Aufgaben.
   * Hier wird ein ThreadPoolTaskExecutor mit einer Kernpoolgröße von 4, einer maximalen Poolgröße von
   * 8 und einer Warteschlangen-Kapazität von 100 erstellt. Die Threads werden mit dem Präfix "Async-" benannt,
   * um sie leichter identifizieren zu können. Der Executor wird initialisiert und zurückgegeben, damit er von
   * Spring verwendet werden kann, um asynchrone Methoden auszuführen.
   *
   * @return Ein konfigurierter Executor für asynchrone Aufgaben.
   */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("Async-");
    executor.initialize();

    return executor;
  }
}
