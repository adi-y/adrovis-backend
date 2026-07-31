package com.adrovis.adrovis_backend.career.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ApplicationRequestValidator.class)
public @interface ValidApplicationRequest {

    String message() default
            "Invalid application request.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}