package io.gitlab.mkjeldsen.prstatbucket;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AwaitTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(AwaitTask.class);

    private final Runnable task;

    public final CountDownLatch countDownLatch;

    public AwaitTask(CountDownLatch countDownLatch, Runnable task) {
        this.task = Objects.requireNonNull(task);
        this.countDownLatch = Objects.requireNonNull(countDownLatch);
    }

    @Override
    public void run() {
        try {
            LOG.info("Waiting for task {}...", task);
            if (countDownLatch.await(3, TimeUnit.SECONDS)) {
                task.run();
            } else {
                LOG.error("Await timed out");
            }
        } catch (InterruptedException e) {
            LOG.error("Await failed", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return "AwaitTask{"
                + "task="
                + task
                + ", countDownLatch="
                + countDownLatch
                + '}';
    }
}
