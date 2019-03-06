package io.gitlab.mkjeldsen.prstatbucket;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TryTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(TryTask.class);

    private final int current;

    private final int attempts;

    private final Runnable task;

    private final Executor executor;

    public TryTask(int attempts, Runnable task, Executor executor) {
        this(1, attempts, task, executor);
    }

    private TryTask(
            int current, int attempts, Runnable task, Executor executor) {

        if (attempts < 1) {
            throw new IllegalArgumentException("attempts < 1");
        }

        this.current = current;
        this.attempts = attempts;
        this.task = Objects.requireNonNull(task);
        this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public void run() {
        if (current >= attempts) {
            LOG.warn("Failed {} attempts of {}", attempts, task);
            return;
        }

        try {
            LOG.info("Try {}/{} of {}...", current, attempts, task);
            task.run();
        } catch (RetriableException re) {
            try {
                executor.execute(retry());
            } catch (RejectedExecutionException e) {
                LOG.error("Rejected {}", task, e);
            }
        }
    }

    private TryTask retry() {
        return new TryTask(current + 1, attempts, task, executor);
    }

    @Override
    public String toString() {
        return "TryTask{"
                + "current="
                + current
                + ", attempts="
                + attempts
                + ", task="
                + task
                + '}';
    }

    public static final class RetriableException extends RuntimeException {
        public RetriableException(Throwable cause) {
            super(cause);
        }
    }
}
