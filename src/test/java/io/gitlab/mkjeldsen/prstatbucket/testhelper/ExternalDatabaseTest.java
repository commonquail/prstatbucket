package io.gitlab.mkjeldsen.prstatbucket.testhelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.test.context.junit.jupiter.EnabledIf;

/**
 * An {@code @ExternalDatabaseTest} requires an external database to be running
 * for the test to be able to succeed. The easiest way to get one such is to run
 * {@code docker-compose up}.
 *
 * <p>But why not <em>Testcontainers</em>? Because Testcontainers assumes a
 * single database user that has DDL permissions.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnabledIf(
        expression = "${test.database:false}",
        reason = "user set test.database=true")
public @interface ExternalDatabaseTest {}
