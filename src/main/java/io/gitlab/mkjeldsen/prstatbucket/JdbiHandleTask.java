package io.gitlab.mkjeldsen.prstatbucket;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Objects;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JdbiHandleTask implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(JdbiHandleTask.class);

    private final Jdbi jdbi;

    private final HandleConsumer<? extends RuntimeException> handleConsumer;

    public JdbiHandleTask(
            Jdbi jdbi,
            HandleConsumer<? extends RuntimeException> handleConsumer) {
        this.jdbi = Objects.requireNonNull(jdbi);
        this.handleConsumer = Objects.requireNonNull(handleConsumer);
    }

    @Override
    public void run() {
        try {
            jdbi.useHandle(handleConsumer);
        } catch (JdbiException e) {
            if (e.getCause() instanceof BatchUpdateException) {
                SQLException nextException =
                        ((BatchUpdateException) e.getCause())
                                .getNextException();
                // FK violation: inserting comment before its owning PR.
                if ("23503".equals(nextException.getSQLState())) {
                    throw new TryTask.RetriableException(e);
                }
            }

            LOG.error("Persist error", e);
        }
    }

    @Override
    public String toString() {
        return "JdbiHandleTask{" + "handleConsumer=" + handleConsumer + '}';
    }
}
