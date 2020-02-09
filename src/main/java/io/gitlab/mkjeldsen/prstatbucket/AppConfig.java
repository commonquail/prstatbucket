package io.gitlab.mkjeldsen.prstatbucket;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.gitlab.mkjeldsen.prstatbucket.duration.DurationDensityEstimateDao;
import io.gitlab.mkjeldsen.prstatbucket.duration.DurationDensityEstimateService;
import io.gitlab.mkjeldsen.prstatbucket.unresolved.UnresolvedReviewDao;
import io.gitlab.mkjeldsen.prstatbucket.unresolved.UnresolvedReviewService;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfig {

    private final Environment env;

    public AppConfig(Environment env) {
        this.env = env;
    }

    @Bean("jsonProvider")
    @Profile("!cache")
    public JsonSupplier liveJsonProvider() {
        return new LiveJsonSupplier(
                env.getProperty("api.client-id"),
                env.getProperty("api.secret"));
    }

    @Bean("jsonProvider")
    @Profile("cache")
    public JsonSupplier recordingJsonProvider(
            @Value("${cache-path}") String cachePath) {
        return new CachingJsonSupplier(
                Path.of(cachePath),
                new LiveJsonSupplier(
                        env.getProperty("api.client-id"),
                        env.getProperty("api.secret")));
    }

    @Bean("jdbi")
    public Jdbi jdbi() {
        final var psql = new PGSimpleDataSource();
        psql.setUser(env.getProperty("db.user"));
        psql.setPassword(env.getProperty("db.password"));
        psql.setDatabaseName(env.getProperty("db.database"));
        psql.setServerNames(new String[] {env.getProperty("db.server")});
        psql.setPortNumbers(
                new int[] {Integer.parseInt(env.getProperty("db.port"))});
        psql.setApplicationName("prstatbucket");

        final var hikariConfig = new HikariConfig();
        hikariConfig.setDataSource(psql);
        final var dataSource = new HikariDataSource(hikariConfig);

        return Jdbi.create(dataSource).installPlugin(new PostgresPlugin());
    }

    @Bean("repositories")
    @ConfigurationProperties("repositories")
    public Collection<String> repositories() {
        // This bean is basically a hack.
        //
        // Lists are flattened to indexed key-value pairs. When two profiles
        // define the same list with a different number of entries,
        // Environment::getProperty does not consider one profile's definition
        // to override the other's, thereby sourcing entries from both lists.
        // Spring's own configuration magic knows how to override, sourcing only
        // from the "highest priority" (seemingly the last) profile.
        //
        // This bean exists to provide the correct repository list override. It
        // is then fed into "repositoryUrls" to be turned into a collection of
        // URLs; because we don't want users to have to deal with that. As a
        // bonus, a client can now access the repository full-name list
        // directly, should they want to.
        //
        // Also, set is more apt but list is cheaper. Just don't repeat
        // repositories.
        return new ArrayList<>();
    }

    @Bean("repositoryUrls")
    public Collection<String> repositoryUrls(Collection<String> repositories) {

        final var repos = new ArrayList<String>(repositories.size());

        for (var repo : repositories) {
            final var url =
                    "https://api.bitbucket.org/2.0/repositories/"
                            + repo
                            + "/pullrequests?state=OPEN&state=MERGED&state=DECLINED";
            repos.add(url);
        }

        return List.copyOf(repos);
    }

    @Bean("executor")
    public ForkJoinPool executor() {
        return new ForkJoinPool();
    }

    @Bean("ingester")
    public Ingester ingester(JsonSupplier jsonSupplier) {
        return new BackgroundIngester(
                PullRequestStateFilter.all(), jsonSupplier, jdbi(), executor());
    }

    @Bean("unresolvedPrIngester")
    public Ingester unresolvedPrIngester(JsonSupplier jsonSupplier) {
        return new BackgroundIngester(
                PullRequestStateFilter.open(),
                jsonSupplier,
                jdbi(),
                executor());
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public UnresolvedReviewService unresolvedReviewDao() {
        return new UnresolvedReviewDao(jdbi(), clock());
    }

    @Bean
    public DurationDensityEstimateService durationDensityEstimateService() {
        return new DurationDensityEstimateDao(jdbi());
    }
}
