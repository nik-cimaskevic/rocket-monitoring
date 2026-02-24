package com.rocket.api.setup.annotations;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Annotation to clean up the DB state before tests execution.
 * Apply to test integration classes which need clean DB state.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(CleanDBStateExtension.class)
public @interface CleanDBState {
}