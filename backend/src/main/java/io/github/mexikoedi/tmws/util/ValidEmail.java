/** Diese Annotation wird verwendet, um sicherzustellen, dass eine E-Mail-Adresse gültig ist. */
package io.github.mexikoedi.tmws.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidEmailValidator.class)
@Documented
public @interface ValidEmail {
  /**
   * Fehlermeldung, die zurückgegeben wird, wenn die Validierung fehlschlägt.
   *
   * @return Die Fehlermeldung.
   */
  String message() default "Ungültige E-Mail-Adresse.";

  /**
   * Gibt die Gruppen an, zu denen diese Validierung gehört.
   *
   * @return Die Gruppen, zu denen diese Validierung gehört.
   */
  Class<?>[] groups() default {};

  /**
   * Gibt die Nutzlast an, die mit dieser Validierung verbunden ist.
   *
   * @return Die Nutzlast, die mit dieser Validierung verbunden ist.
   */
  Class<? extends Payload>[] payload() default {};
}
