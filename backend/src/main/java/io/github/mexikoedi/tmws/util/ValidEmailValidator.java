/**
 * Diese Klasse implementiert die Validierung, um sicherzustellen, dass eine E-Mail-Adresse gültig
 * ist.
 */
package io.github.mexikoedi.tmws.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  /**
   * Validiert, dass die angegebene E-Mail-Adresse einem gültigen Format entspricht.
   *
   * @param value Die E-Mail-Adresse, die validiert werden soll.
   * @param context Der Kontext, in dem die Validierung stattfindet, ermöglicht das Hinzufügen von
   *     benutzerdefinierten Fehlermeldungen.
   * @return true, wenn die E-Mail-Adresse gültig ist oder null ist, andernfalls false.
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    return EMAIL_PATTERN.matcher(value).matches();
  }
}
