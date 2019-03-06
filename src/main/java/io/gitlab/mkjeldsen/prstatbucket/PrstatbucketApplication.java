package io.gitlab.mkjeldsen.prstatbucket;

import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class PrstatbucketApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PrstatbucketApplication.class, args);
    }

    @EventListener
    public void migrateDatabase(final ApplicationStartedEvent e) {
        final Environment env = e.getApplicationContext().getEnvironment();
        final String user = env.getProperty("flyway.user");
        final String password = env.getProperty("flyway.password");
        final String server = env.getProperty("db.server");
        final int port = Integer.parseInt(env.getProperty("db.port"));
        final String database = env.getProperty("db.database");

        final var psql = new PGSimpleDataSource();
        psql.setServerName(server);
        psql.setPortNumber(port);
        psql.setDatabaseName(database);
        psql.setUser(user);
        psql.setPassword(password);

        final Flyway flyway = Flyway.configure().dataSource(psql).load();
        flyway.migrate();
    }
}
