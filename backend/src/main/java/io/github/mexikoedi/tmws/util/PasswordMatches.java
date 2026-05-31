/**
 * Diese Annotation wird verwendet, um sicherzustellen, dass die Passwörter übereinstimmen.
 */
package io.github.mexikoedi.tmws.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Documented
public @interface PasswordMatches {
  /**
   * Fehlermeldung, die zurückgegeben wird, wenn die Validierung fehlschlägt.
   *
   * @return Die Fehlermeldung.
   */
  String message() default "Passwörter stimmen nicht überein.";

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
