/**
 * Diese Klasse enthält Unit-Tests für die TokenGenerator-Klasse, die für die Generierung von
 * Authentifizierungs-Token verantwortlich ist. Die Tests überprüfen, ob die generierten Token
 * gültige UUID-Strings sind und ob sie eindeutig sind, um sicherzustellen, dass keine Duplikate
 * entstehen.
 */
package io.github.mexikoedi.tmws.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TokenGenerator Tests")
class TokenGeneratorTest {
  @Test
  @DisplayName("generateToken - Sollte einen gültigen UUID-String zurückgeben.")
  void generateToken_shouldReturnValidUUID() {
    String token = TokenGenerator.generateToken();
    assertNotNull(token);
    assertFalse(token.isBlank());
    assertDoesNotThrow(() -> UUID.fromString(token));
  }

  @Test
  @DisplayName("generateToken - Sollte eindeutige Tokens generieren.")
  void generateToken_shouldBeUnique() {
    String token1 = TokenGenerator.generateToken();
    String token2 = TokenGenerator.generateToken();
    assertNotEquals(token1, token2);
  }
}
