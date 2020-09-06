package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;

public final class Savepoint {

    private final int uId;

    private final int prId;

    private final int cId;

    private final int aId;

    private Savepoint(int uId, int prId, int cId, int aId) {
        this.uId = uId;
        this.prId = prId;
        this.cId = cId;
        this.aId = aId;
    }

    public static Savepoint create(Handle handle) {
        return handle.select(
                        "SELECT "
                                + " (SELECT max(u_id) FROM bitbucket_user) u_id,"
                                + " (SELECT max(pr_id) FROM pull_request) pr_id,"
                                + " (SELECT max(c_id) FROM pull_request_comment) c_id,"
                                + " (SELECT max(a_id) FROM pull_request_approval) a_id")
                .map(Savepoint::map)
                .first();
    }

    private static Savepoint map(ResultSet rs, StatementContext ctx)
            throws SQLException {
        return new Savepoint(
                rs.getInt("u_id"),
                rs.getInt("pr_id"),
                rs.getInt("c_id"),
                rs.getInt("a_id"));
    }

    public void restore(Jdbi jdbi) {
        jdbi.useHandle(this::doRestore);
    }

    private void doRestore(Handle handle) {
        handle.execute("DELETE FROM pull_request_approval WHERE a_id > ?", aId);
        handle.execute("DELETE FROM pull_request_comment WHERE c_id > ?", cId);
        handle.execute("DELETE FROM pull_request WHERE pr_id > ?", prId);
        handle.execute("DELETE FROM bitbucket_user WHERE u_id > ?", uId);
    }
}
