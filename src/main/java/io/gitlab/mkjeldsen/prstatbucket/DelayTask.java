package io.gitlab.mkjeldsen.prstatbucket;

import java.time.Duration;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DelayTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DelayTask.class);

    private final Duration delay;

    private final Runnable task;

    public DelayTask(Duration delay, Runnable task) {
        this.delay = Objects.requireNonNull(delay);
        this.task = Objects.requireNonNull(task);
    }

    @Override
    public void run() {
        try {
            LOG.info("Delaying for {} task {}...", delay, task);
            Thread.sleep(delay.toMillis());
            task.run();
        } catch (InterruptedException e) {
            LOG.error("Delay failed", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return "DelayTask{" + "delay=" + delay + ", task=" + task + '}';
    }
}
