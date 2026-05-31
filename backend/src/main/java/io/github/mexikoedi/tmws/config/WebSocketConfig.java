/**
 * Diese Klasse konfiguriert die WebSocket-Verbindung für die Anwendung.
 * Sie ermöglicht die Kommunikation zwischen dem Server und den Clients über das STOMP-Protokoll.
 */
package io.github.mexikoedi.tmws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  /**
   * Registriert den STOMP-Endpunkt für die WebSocket-Verbindung.
   * Clients können sich über diesen Endpunkt verbinden.
   *
   * @param registry Der StompEndpointRegistry, um den Endpunkt zu registrieren.
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/api/ws").setAllowedOriginPatterns("http://localhost:4200");
  }

  /**
   * Konfiguriert den Message Broker für die WebSocket-Verbindung.
   * Der Broker ermöglicht die Kommunikation zwischen dem Server und den Clients über das STOMP-Protokoll.
   *
   * @param registry Der MessageBrokerRegistry, um den Broker zu konfigurieren.
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic").setHeartbeatValue(new long[]{10000, 10000}).setTaskScheduler(heartBeatScheduler());
    registry.setApplicationDestinationPrefixes("/app");
  }

  /**
   * Erstellt einen TaskScheduler für die Herzschlag-Funktion des Message Brokers.
   *
   * @return Ein TaskScheduler-Bean, der für die Herzschlag-Funktion verwendet wird.
   */
  @Bean
  public TaskScheduler heartBeatScheduler() {
    return new ThreadPoolTaskScheduler();
  }
}
