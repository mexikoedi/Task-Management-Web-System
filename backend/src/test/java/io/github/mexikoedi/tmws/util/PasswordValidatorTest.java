package io.github.mexikoedi.tmws.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PasswordValidator Tests")
class PasswordValidatorTest {

  @Test
  @DisplayName("accepts strong password")
  void acceptsStrongPassword() {
    assertTrue(PasswordValidator.isValid("Str0ng!Pass"));
  }

  @Test
  @DisplayName("rejects password without special char")
  void rejectsMissingSpecialCharacter() {
    assertFalse(PasswordValidator.isValid("Str0ngPass"));
  }

  @Test
  @DisplayName("rejects too short password")
  void rejectsShortPassword() {
    assertFalse(PasswordValidator.isValid("Aa1!"));
  }

  @Test
  @DisplayName("rejects null password")
  void rejectsNullPassword() {
    assertFalse(PasswordValidator.isValid(null));
  }
}
