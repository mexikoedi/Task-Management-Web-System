/** Diese Klasse ist verantwortlich für die Erstellung und Validierung von JWTs in der Anwendung. */
package io.github.mexikoedi.tmws.security;

import io.github.mexikoedi.tmws.exception.JwtExpiredException;
import io.github.mexikoedi.tmws.exception.JwtInvalidException;
import io.github.mexikoedi.tmws.exception.JwtMalformedException;
import io.github.mexikoedi.tmws.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  private SecretKey signingKey;

  /**
   * Generiert ein JWT für den angegebenen Benutzer. Das Token enthält die E-Mail des Benutzers als
   * Subject und die aktuelle Token-Version als Claim. Das Token ist für die in der Konfiguration
   * angegebene Dauer gültig und wird mit dem definierten SecretKey signiert.
   *
   * @param user Der Benutzer, für den das JWT generiert werden soll.
   * @return Das generierte JWT als String.
   */
  public String generateToken(User user) {
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("tokenVersion", user.getTokenVersion())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(signingKey, Jwts.SIG.HS256)
        .compact();
  }

  /**
   * Parst das übergebene JWT und gibt die Claims zurück, wenn das Token gültig ist.
   *
   * @param token Das JWT, das geparst und validiert werden soll.
   * @throws JwtExpiredException Wenn das JWT abgelaufen ist.
   * @throws JwtMalformedException Wenn das JWT ungültig oder manipuliert ist.
   * @throws JwtInvalidException Wenn das JWT nicht verarbeitet werden kann.
   * @return Die Claims des gültigen JWTs.
   */
  public Claims getClaims(String token) {
    try {
      return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException _) {
      throw new JwtExpiredException("JWT ist abgelaufen.");
    } catch (MalformedJwtException _) {
      throw new JwtMalformedException("JWT ist ungültig oder manipuliert.");
    } catch (Exception _) {
      throw new JwtInvalidException("JWT konnte nicht verarbeitet werden.");
    }
  }

  /**
   * Validiert das übergebene JWT, indem es versucht, die Claims zu parsen.
   *
   * @param token Das JWT, das validiert werden soll.
   * @return true, wenn das JWT gültig ist, andernfalls false.
   */
  public boolean validateToken(String token) {
    try {
      getClaims(token);

      return true;
    } catch (JwtExpiredException | JwtMalformedException | JwtInvalidException _) {
      return false;
    }
  }

  /**
   * Extrahiert die E-Mail-Adresse des Benutzers aus dem übergebenen JWT, indem die Claims geparst
   * werden.
   *
   * @param token Das JWT, aus dem die E-Mail-Adresse extrahiert werden soll.
   * @return Die E-Mail-Adresse des Benutzers, wenn das JWT gültig ist, andernfalls null.
   */
  public String getEmailFromToken(String token) {
    try {
      return getClaims(token).getSubject();
    } catch (JwtExpiredException | JwtMalformedException | JwtInvalidException _) {
      return null;
    }
  }

  /** Initialisiert den SecretKey für die JWT-Signierung nach dem Laden der Konfiguration. */
  @PostConstruct
  private void init() {
    this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }
}
