package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import java.util.function.Consumer;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;

public final class RollbackTransaction
        implements HandleConsumer<RuntimeException> {

    private final Consumer<Handle> body;

    public RollbackTransaction(Consumer<Handle> body) {
        this.body = body;
    }

    @Override
    public void useHandle(final Handle handle) {
        try {
            handle.setTransactionIsolation(
                    TransactionIsolationLevel.READ_UNCOMMITTED);
            handle.begin();
            body.accept(handle);
        } finally {
            handle.rollback();
        }
    }
}
