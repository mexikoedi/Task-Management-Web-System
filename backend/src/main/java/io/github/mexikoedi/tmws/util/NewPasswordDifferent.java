/**
 * Diese Annotation wird verwendet, um sicherzustellen, dass das neue Passwort eines Benutzers sich vom
 * aktuellen Passwort unterscheidet.
 */
package io.github.mexikoedi.tmws.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NewPasswordDifferentValidator.class)
@Documented
public @interface NewPasswordDifferent {
  /**
   * Fehlermeldung, die zurückgegeben wird, wenn die Validierung fehlschlägt.
   *
   * @return Die Fehlermeldung.
   */
  String message() default "Neues Passwort muss sich vom aktuellen unterscheiden.";

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
