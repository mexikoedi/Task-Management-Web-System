package io.github.mexikoedi.tmws.util;

import io.github.mexikoedi.tmws.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider - Handles creation and validation of JWT tokens.
 *
 * <p>This component provides methods to generate and validate JWT tokens using HMAC-SHA256
 * algorithm. The secret key and expiration time can be configured via properties.
 */
@Component
public class JwtTokenProvider {
  @Value(
      "${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidationMustBeAtLeast256bitsForHS256Algorithm}")
  private String jwtSecret;

  @Value("${jwt.expiration:86400000}")
  private int jwtExpiration;

  /**
   * Creates a SecretKey from the configured JWT secret string.
   *
   * @return the HMAC-SHA256 secret key
   */
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  /**
   * Generates a JWT token for the given user.
   *
   * @param user the user to create a token for
   * @return the generated JWT token string
   */
  public String generateToken(User user) {
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("tokenVersion", user.getTokenVersion())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(getSigningKey(), Jwts.SIG.HS256)
        .compact();
  }

  /**
   * Extracts claims from a JWT token.
   *
   * @param token the JWT token string
   * @return the Claims object containing token payload
   */
  public Claims getClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Validates if a JWT token is valid and not expired.
   *
   * @param token the JWT token string to validate
   * @return true if token is valid, false otherwise
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
