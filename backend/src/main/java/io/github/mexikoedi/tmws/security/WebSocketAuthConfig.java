/**
 * Diese Klasse konfiguriert die WebSocket-Sicherheit, indem sie Interceptoren für eingehende
 * Nachrichten registriert.
 */
package io.github.mexikoedi.tmws.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {
  private final WebSocketJwtInterceptor webSocketjwtInterceptor;
  private final WebSocketTopicInterceptor webSocketTopicInterceptor;

  /**
   * Konstruktor für die WebSocketAuthConfig-Klasse, der die benötigten Interceptoren injiziert.
   *
   * @param webSocketjwtInterceptor Der Interceptor, der JWT-Token in WebSocket-Nachrichten
   *     überprüft.
   * @param webSocketTopicInterceptor Der Interceptor, der die Berechtigungen für WebSocket-Themen
   *     überprüft.
   */
  public WebSocketAuthConfig(
      WebSocketJwtInterceptor webSocketjwtInterceptor,
      WebSocketTopicInterceptor webSocketTopicInterceptor) {
    this.webSocketjwtInterceptor = webSocketjwtInterceptor;
    this.webSocketTopicInterceptor = webSocketTopicInterceptor;
  }

  /**
   * Konfiguriert den Channel für eingehende WebSocket-Nachrichten, indem die definierten
   * Interceptoren hinzugefügt werden.
   *
   * @param registration Die ChannelRegistration, die verwendet wird, um die Interceptoren zu
   *     registrieren.
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(webSocketjwtInterceptor, webSocketTopicInterceptor);
  }
}
