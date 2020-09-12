package io.gitlab.mkjeldsen.prstatbucket.testhelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@code @FalseExternalDatabaseTest} is a test that is not an {@link
 * ExternalDatabaseTest @ExternalDatabaseTest} yet has the same prerequisite;
 * usually implicitly. It exists because the application configuration presently
 * is not sophisticated enough to sidestep those dependencies and accomplishing
 * that unlocks comparatively few, low-criticality tests.
 */
@ExternalDatabaseTest
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FalseExternalDatabaseTest {}
