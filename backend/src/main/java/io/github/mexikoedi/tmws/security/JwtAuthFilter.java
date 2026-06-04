/**
 * Diese Klasse ist ein Filter, der bei jeder Anfrage überprüft, ob ein gültiges JWT-Token im
 * Authorization-Header vorhanden ist.
 */
package io.github.mexikoedi.tmws.security;

import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.repository.UserRepository;
import io.github.mexikoedi.tmws.security.exception.InvalidTokenException;
import io.github.mexikoedi.tmws.security.exception.JwtExpiredException;
import io.github.mexikoedi.tmws.security.exception.JwtInvalidException;
import io.github.mexikoedi.tmws.security.exception.JwtMalformedException;
import io.github.mexikoedi.tmws.service.exception.ResourceNotFoundException;
import io.github.mexikoedi.tmws.service.exception.UserDeactivatedException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;

  /**
   * Konstruktor für den JwtAuthFilter, der die benötigten Abhängigkeiten injiziert.
   *
   * @param userRepository Der UserRepository, um Benutzerdaten aus der Datenbank abzurufen.
   * @param jwtProvider Der JwtProvider, um JWT-Token zu validieren und Claims zu extrahieren.
   */
  public JwtAuthFilter(UserRepository userRepository, JwtProvider jwtProvider) {
    this.userRepository = userRepository;
    this.jwtProvider = jwtProvider;
  }

  /**
   * Diese Methode wird bei jeder Anfrage aufgerufen und überprüft, ob ein gültiges JWT-Token im
   * Authorization-Header vorhanden ist. Wenn ja, wird die Authentifizierung im SecurityContext
   * gesetzt. Andernfalls wird die Anfrage ohne Authentifizierung weitergeleitet.
   *
   * @param request Der HttpServletRequest, der die eingehende Anfrage repräsentiert.
   * @param response Der HttpServletResponse, der die Antwort repräsentiert.
   * @param filterChain Der FilterChain, um die Anfrage weiterzuleiten, wenn kein gültiges Token
   *     gefunden wird.
   * @throws ServletException Wenn ein Fehler bei der Verarbeitung der Anfrage auftritt.
   * @throws IOException Wenn ein Fehler bei der Ein- oder Ausgabe auftritt.
   * @throws ResourceNotFoundException Wenn der Benutzer, der im Token angegeben ist, nicht gefunden
   *     wird.
   * @throws UserDeactivatedException Wenn der Benutzer, der im Token angegeben ist, deaktiviert
   *     ist.
   * @throws InvalidTokenException Wenn das Token ungültig ist.
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String token = extractTokenFromRequest(request);

    if (token != null) {
      try {
        Claims claims = jwtProvider.getClaims(token);
        String email = claims.getSubject();
        Integer tokenVersion = claims.get("tokenVersion", Integer.class);
        User user =
            userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden."));

        if (!user.isEnabled()) {
          throw new UserDeactivatedException("Benutzer ist deaktiviert.");
        }

        if (user.getTokenVersion() != tokenVersion) {
          throw new InvalidTokenException("Token ist ungültig.");
        }

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (ResourceNotFoundException
          | UserDeactivatedException
          | InvalidTokenException
          | JwtExpiredException
          | JwtMalformedException
          | JwtInvalidException _) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Diese Methode extrahiert das JWT-Token aus dem Authorization-Header der Anfrage. Es wird
   * erwartet, dass der Header im Format "Bearer <token>" vorliegt.
   *
   * @param request Der HttpServletRequest, der die eingehende Anfrage repräsentiert.
   * @return Das extrahierte JWT-Token oder null, wenn kein gültiger Authorization-Header gefunden
   *     wurde.
   */
  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearer = request.getHeader("Authorization");

    return (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
  }
}
