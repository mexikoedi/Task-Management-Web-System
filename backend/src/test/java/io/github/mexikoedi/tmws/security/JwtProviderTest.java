/**
 * Diese Klasse enthält Unit-Tests für die JwtProvider-Klasse, die für die Generierung und Validierung
 * von JWT-Token verantwortlich ist.
 * Die Tests überprüfen, ob die generierten Token gültig sind, ob die Claims korrekt extrahiert werden können und
 * ob die Validierung von gültigen und ungültigen Token korrekt funktioniert.
 */
package io.github.mexikoedi.tmws.security;

import static org.junit.jupiter.api.Assertions.*;
import io.github.mexikoedi.tmws.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"jwt.secret=mySuperSecretKeyThatIsAtLeast32CharactersLongForHS256", "jwt.expiration=3600000"})
@DisplayName("JwtProvider Tests")
class JwtProviderTest {
  @Autowired private JwtProvider jwtProvider;
  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");
    testUser.setTokenVersion(1);
  }

  @Test
  @DisplayName("generateToken - Sollte ein gültiges JWT-Token zurückgeben.")
  void testGenerateToken() {
    String token = jwtProvider.generateToken(testUser);
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.contains("."));
  }

  @Test
  @DisplayName("getClaims - Sollte die richtigen Claims aus dem Token extrahieren.")
  void testGetClaims() {
    String token = jwtProvider.generateToken(testUser);
    Claims claims = jwtProvider.getClaims(token);
    assertNotNull(claims);
    assertEquals(testUser.getEmail(), claims.getSubject());
    assertEquals(testUser.getTokenVersion(), claims.get("tokenVersion"));
  }

  @Test
  @DisplayName("validateToken - Sollte ein gültiges Token als gültig erkennen.")
  void testValidateToken() {
    String token = jwtProvider.generateToken(testUser);
    boolean isValid = jwtProvider.validateToken(token);
    assertTrue(isValid);
  }

  @Test
  @DisplayName("validateToken - Sollte ein ungültiges Token als ungültig erkennen.")
  void testValidateInvalidToken() {
    String invalidToken = "ungültiger.token.hier";
    boolean isValid = jwtProvider.validateToken(invalidToken);
    assertFalse(isValid);
  }

  @Test
  @DisplayName("validateToken - Sollte ein manipuliertes Token als ungültig erkennen.")
  void testValidateTamperedToken() {
    String token = jwtProvider.generateToken(testUser);
    String tamperedToken = token.substring(0, token.length() - 5);
    boolean isValid = jwtProvider.validateToken(tamperedToken);
    assertFalse(isValid);
  }

  @Test
  @DisplayName("getEmailFromToken - Sollte die richtige E-Mail aus dem Token extrahieren.")
  void testGetEmailFromToken() {
    String token = jwtProvider.generateToken(testUser);
    String email = jwtProvider.getEmailFromToken(token);
    assertEquals(testUser.getEmail(), email);
  }
}
