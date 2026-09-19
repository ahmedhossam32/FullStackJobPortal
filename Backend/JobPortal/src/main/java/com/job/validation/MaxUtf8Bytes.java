package com.job.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a String's UTF-8 byte length does not exceed {@link #value()}. Unlike
 * {@code @Size}, which counts characters, this counts encoded bytes -- needed for fields (like a
 * BCrypt-hashed password) whose underlying limit is byte-based, not character-based. Null is
 * valid; combine with {@code @NotBlank}/{@code @NotNull} to also reject null.
 */
@Documented
@Constraint(validatedBy = MaxUtf8BytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxUtf8Bytes {

    int value();

    String message() default "must be at most {value} bytes long";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
