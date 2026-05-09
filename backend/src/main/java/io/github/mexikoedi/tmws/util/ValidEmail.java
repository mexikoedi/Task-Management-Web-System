package io.github.mexikoedi.tmws.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidEmailValidator.class)
@Documented
public @interface ValidEmail {
    String message() default "E-Mail-Adresse muss gültig sein (z.B. benutzer@beispiel.de)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

