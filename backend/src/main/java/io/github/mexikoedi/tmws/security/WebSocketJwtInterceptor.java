/**
 * Interceptor, der JWT-Token aus WebSocket-Verbindungsanfragen extrahiert und validiert.
 */
package io.github.mexikoedi.tmws.security;

import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Objects;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {
  private final JwtProvider tokenProvider;
  private final UserRepository userRepository;

  /**
   * Konstruktor für den WebSocketJwtInterceptor, der die benötigten Abhängigkeiten injiziert.
   *
   * @param tokenProvider Der JwtProvider, um JWT-Token zu validieren und Claims zu extrahieren.
   * @param userRepository Der UserRepository, um Benutzerdaten aus der Datenbank abzurufen.
   */
  public WebSocketJwtInterceptor(JwtProvider tokenProvider, UserRepository userRepository) {
    this.tokenProvider = tokenProvider;
    this.userRepository = userRepository;
  }

  /**
   * Diese Methode wird bei jeder WebSocket-Verbindungsanfrage aufgerufen und überprüft, ob ein gültiges
   * JWT-Token im Authorization-Header vorhanden ist.
   *
   * @param message Die eingehende Nachricht, die die Verbindungsanfrage repräsentiert.
   * @param channel Der MessageChannel, über den die Nachricht gesendet wird.
   * @return Die ursprüngliche Nachricht, wenn ein gültiges Token gefunden und die Authentifizierung erfolgreich
   * gesetzt wurde, oder null, wenn die Verbindung abgelehnt werden soll.
   */
  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String authHeader = accessor.getFirstNativeHeader("Authorization");

      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return null;
      }

      String token = authHeader.substring(7);

      if (!tokenProvider.validateToken(token)) {
        return null;
      }

      String email = tokenProvider.getEmailFromToken(token);

      if (email == null) {
        return null;
      }

      User user = userRepository.findByEmail(email).orElse(null);

      if (user == null) {
        return null;
      }

      if (!user.isEnabled()) {
        return null;
      }

      Claims claims = tokenProvider.getClaims(token);
      Integer tokenVersion = claims.get("tokenVersion", Integer.class);

      if (user.getTokenVersion() != tokenVersion) {
        return null;
      }

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
      accessor.setUser(auth);
      accessor.setLeaveMutable(true);
      Objects.requireNonNull(accessor.getSessionAttributes()).put("SPRING.AUTHENTICATION", auth);
    }

    return message;
  }
}
