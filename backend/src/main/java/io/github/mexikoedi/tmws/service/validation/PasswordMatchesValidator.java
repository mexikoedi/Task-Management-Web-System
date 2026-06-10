package io.github.mexikoedi.tmws.service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator
    implements ConstraintValidator<PasswordMatches, PasswordConfirmation> {
  @Override
  public boolean isValid(PasswordConfirmation dto, ConstraintValidatorContext context) {
    if (dto == null) {
      return true;
    }

    boolean matches =
        dto.getPassword() != null && dto.getPassword().equals(dto.getPasswordConfirm());

    if (!matches) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Passwörter stimmen nicht überein.")
          .addPropertyNode(dto.getPasswordConfirmFieldName())
          .addConstraintViolation();
    }

    return matches;
  }
}
