/**
 * Diese Klasse ist ein Interceptor für WebSocket-Nachrichten, der sicherstellt, dass Benutzer nur
 * auf ihre eigenen Themen zugreifen können.
 */
package io.github.mexikoedi.tmws.security;

import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class WebSocketTopicInterceptor implements ChannelInterceptor {
  private final UserRepository userRepository;

  /**
   * Konstruktor für den WebSocketTopicInterceptor, der die benötigten Abhängigkeiten injiziert.
   *
   * @param userRepository Der UserRepository, um Benutzerdaten aus der Datenbank abzurufen.
   */
  public WebSocketTopicInterceptor(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Diese Methode wird bei jeder WebSocket-Nachricht aufgerufen und überprüft, ob ein Benutzer
   * versucht, auf ein Thema zuzugreifen, das nicht ihm gehört. Wenn dies der Fall ist, wird die
   * Nachricht verworfen, indem null zurückgegeben wird. Andernfalls wird die ursprüngliche
   * Nachricht zurückgegeben, damit sie weiterverarbeitet werden kann.
   *
   * @param message Die eingehende Nachricht, die überprüft werden soll.
   * @param channel Der MessageChannel, über den die Nachricht gesendet wird.
   * @return Die ursprüngliche Nachricht, wenn der Zugriff erlaubt ist, oder null, wenn die
   *     Nachricht verworfen werden soll.
   */
  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      String destination = accessor.getDestination();
      Authentication auth = (Authentication) accessor.getUser();

      if (destination == null || auth == null) {
        return message;
      }

      String email = (String) auth.getPrincipal();

      User user = userRepository.findByEmail(email).orElse(null);

      if (user == null) {
        return null;
      }

      if (destination.startsWith("/topic/user/")) {
        Long id = Long.parseLong(destination.substring("/topic/user/".length()));

        if (!id.equals(user.getId())) {
          return null;
        }
      }
    }

    return message;
  }
}
