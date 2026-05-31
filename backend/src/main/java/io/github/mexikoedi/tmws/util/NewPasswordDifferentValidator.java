/**
 * Diese Klasse implementiert die Validierung, um sicherzustellen, dass das neue Passwort eines
 * Benutzers sich vom aktuellen Passwort unterscheidet.
 */
package io.github.mexikoedi.tmws.util;

import io.github.mexikoedi.tmws.dto.UpdatePasswordRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NewPasswordDifferentValidator
    implements ConstraintValidator<NewPasswordDifferent, UpdatePasswordRequest> {
  /**
   * Validiert, dass das neue Passwort sich vom aktuellen Passwort unterscheidet.
   *
   * @param dto Das UpdatePasswordRequest-Objekt, das die aktuellen und neuen Passwörter enthält.
   * @param context Der Kontext, in dem die Validierung stattfindet, ermöglicht das Hinzufügen von
   *     benutzerdefinierten Fehlermeldungen.
   * @return true, wenn das neue Passwort sich vom aktuellen Passwort unterscheidet, andernfalls
   *     false.
   */
  @Override
  public boolean isValid(UpdatePasswordRequest dto, ConstraintValidatorContext context) {
    if (dto == null) {
      return true;
    }

    boolean different =
        dto.getCurrentPassword() != null
            && dto.getNewPassword() != null
            && !dto.getCurrentPassword().equals(dto.getNewPassword());

    if (!different) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Neues Passwort muss sich vom aktuellen unterscheiden.")
          .addPropertyNode("newPassword")
          .addConstraintViolation();
    }

    return different;
  }
}
