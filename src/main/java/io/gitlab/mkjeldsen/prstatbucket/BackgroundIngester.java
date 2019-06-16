package io.gitlab.mkjeldsen.prstatbucket;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequest;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequestActivity;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequests;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An ingester that performs all its work on background threads. */
public final class BackgroundIngester implements Ingester {

    private static final Logger LOG =
            LoggerFactory.getLogger(BackgroundIngester.class);

    private final Jdbi jdbi;

    private final ForkJoinPool executor;

    private final JsonSupplier jsonSupplier;

    private final ObjectMapper objectMapper;

    public BackgroundIngester(
            JsonSupplier jsonSupplier, Jdbi jdbi, ForkJoinPool executor) {
        this.jdbi = jdbi;
        this.executor = executor;
        this.jsonSupplier = jsonSupplier;
        this.objectMapper = objectMapper();
    }

    static ObjectMapper objectMapper() {
        final var domainModule = new SimpleModule();
        domainModule.addDeserializer(
                PullRequestActivity.class,
                new DeserializePullRequestActivity());

        final var om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.registerModule(domainModule);
        om.enable(
                DeserializationFeature
                        .READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        return om;
    }

    private static String extractActivityUrl(PullRequest pr) {
        return pr.links.activity.href;
    }

    @Override
    public void ingestAll(Collection<String> urls) {
        urls.forEach(this::ingest);
    }

    @Override
    public void ingest(String url) {
        try {
            executor.execute(() -> doIngest(url));
        } catch (RejectedExecutionException e) {
            LOG.error("Rejected '{}'", url, e);
        }
    }

    @Override
    public boolean isBusy() {
        return !executor.isQuiescent();
    }

    private void doIngest(String url) {

        try {
            final Optional<String> maybeNext;

            if (!url.contains("/activity")) {
                PullRequests prs =
                        objectMapper
                                .readerFor(PullRequests.class)
                                .readValue(jsonSupplier.getJson(url));

                upsert(prs);

                prs.values
                        .stream()
                        .map(BackgroundIngester::extractActivityUrl)
                        .forEach(this::ingest);

                maybeNext = prs.getNext();
            } else {
                PullRequestActivity activity =
                        objectMapper
                                .readerFor(PullRequestActivity.class)
                                .readValue(jsonSupplier.getJson(url));

                upsert(activity);

                maybeNext = activity.getNext();
            }

            maybeNext.ifPresent(this::ingest);
        } catch (IOException e) {
            LOG.error("Failed to ingest {}", url, e);
        }
    }

    private void upsert(PullRequests prs) {
        final var handleTask = new PullRequestHandleConsumer(prs);
        final var jdbiTask = new JdbiHandleTask(jdbi, handleTask);
        final var tryTask = new TryTask(3, jdbiTask, executor);
        exec(tryTask);
    }

    private void upsert(PullRequestActivity pullRequestActivity) {
        final var handleTask = new ActivityHandleConsumer(pullRequestActivity);
        final var jdbiTask = new JdbiHandleTask(jdbi, handleTask);
        final var tryTask = new TryTask(3, jdbiTask, executor);
        exec(tryTask);
    }

    private void exec(Runnable task) {
        try {
            executor.execute(task);
        } catch (RejectedExecutionException e) {
            LOG.error("Rejected {}", task, e);
        }
    }
}
