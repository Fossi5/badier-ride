package com.badier.badier_ride.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndAfterStartValidator.class)
public @interface EndAfterStart {
    String message() default "La date de fin doit être postérieure à la date de début";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
