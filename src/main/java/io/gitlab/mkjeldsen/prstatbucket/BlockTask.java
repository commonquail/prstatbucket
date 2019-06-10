package io.gitlab.mkjeldsen.prstatbucket;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlockTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BlockTask.class);

    private final Runnable task;

    public final CountDownLatch countDownLatch;

    public BlockTask(Runnable task, CountDownLatch countDownLatch) {
        this.task = Objects.requireNonNull(task);
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        task.run();
        LOG.info("Delaying for {} task {}...", countDownLatch, task);
        countDownLatch.countDown();
    }

    @Override
    public String toString() {
        return "BlockTask{"
                + "task="
                + task
                + ", countDownLatch="
                + countDownLatch
                + '}';
    }
}
