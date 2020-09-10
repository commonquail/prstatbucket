package io.gitlab.mkjeldsen.prstatbucket.testhelper;

import static org.assertj.core.api.Assertions.fail;

import io.gitlab.mkjeldsen.prstatbucket.BackgroundIngester;
import io.gitlab.mkjeldsen.prstatbucket.Ingester;
import io.gitlab.mkjeldsen.prstatbucket.PullRequestStateFilter;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;

public final class TestIngester implements Ingester {

    private final ForkJoinPool executor = new ForkJoinPool();
    private final BackgroundIngester ingester;

    public TestIngester(
            final Jdbi jdbi, final Function<String, String> override) {
        ingester =
                new BackgroundIngester(
                        PullRequestStateFilter.all(),
                        new TestResourceJsonSupplier(override),
                        jdbi,
                        executor);
    }

    @Override
    public void ingestAll(final Collection<String> urls) {
        invoke(urls);
    }

    @Override
    public void ingest(final String url) {
        invoke(List.of(url));
    }

    @Override
    public boolean isBusy() {
        return ingester.isBusy();
    }

    private void invoke(final Collection<String> urls) {
        ingester.ingestAll(urls);
        if (!executor.awaitQuiescence(2, TimeUnit.SECONDS)) {
            fail("tardy executor");
        }
    }
}
