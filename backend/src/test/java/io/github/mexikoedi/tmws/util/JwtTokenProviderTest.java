package io.github.mexikoedi.tmws.util;

import io.github.mexikoedi.tmws.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 *
 * <p>Tests JWT token generation, validation, and claims extraction.
 */
@SpringBootTest
@TestPropertySource(properties = {"jwt.secret=mySuperSecretKeyThatIsAtLeast32CharactersLongForHS256", "jwt.expiration=3600000"})
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

  @Autowired private JwtTokenProvider jwtTokenProvider;

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
  @DisplayName("Should generate valid JWT token")
  void testGenerateToken() {
    String token = jwtTokenProvider.generateToken(testUser);
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.contains("."));
  }

  @Test
  @DisplayName("Should extract claims from valid token")
  void testGetClaims() {
    String token = jwtTokenProvider.generateToken(testUser);
    Claims claims = jwtTokenProvider.getClaims(token);

    assertNotNull(claims);
    assertEquals(testUser.getEmail(), claims.getSubject());
    assertEquals(testUser.getTokenVersion(), claims.get("tokenVersion"));
  }

  @Test
  @DisplayName("Should validate token successfully")
  void testValidateToken() {
    String token = jwtTokenProvider.generateToken(testUser);
    boolean isValid = jwtTokenProvider.validateToken(token);

    assertTrue(isValid);
  }

  @Test
  @DisplayName("Should reject invalid token")
  void testValidateInvalidToken() {
    String invalidToken = "invalid.token.here";
    boolean isValid = jwtTokenProvider.validateToken(invalidToken);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should reject tampered token")
  void testValidateTamperedToken() {
    String token = jwtTokenProvider.generateToken(testUser);
    String tamperedToken = token.substring(0, token.length() - 5);
    boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

    assertFalse(isValid);
  }
}

