package io.gitlab.mkjeldsen.prstatbucket.duration;

import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.mkjeldsen.prstatbucket.unresolved.RollbackTransaction;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.EnabledIf;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@EnabledIf("${smoke.tests.enabled:false}")
final class DurationDensityEstimateServiceTest {

    @Autowired
    private Jdbi jdbi;

    @Autowired
    @Qualifier("durationDensityEstimateService")
    private DurationDensityEstimateService service;

    private static final String INSERT_PR =
            "INSERT INTO pull_request ("
                    + " pr_url,"
                    + " title,"
                    + " author,"
                    + " state,"
                    + " created_ts,"
                    + " closed_ts)"
                    + " VALUES ("
                    + " md5(random()::text),"
                    + " md5(random()::text),"
                    + " md5(random()::text),"
                    + " :state,"
                    + " :created_ts AT TIME ZONE 'UTC',"
                    + " :closed_ts AT TIME ZONE 'UTC')";

    private static final String INSERT_APPROVAL =
            "INSERT INTO pull_request_approval ("
                    + " pr_url,"
                    + " approver,"
                    + " approval_ts)"
                    + " SELECT"
                    + " p.pr_url,"
                    + " md5(random()::text),"
                    + " p.created_ts + CAST(:approval_ts_offset AS INTERVAL)"
                    + " FROM pull_request AS p";

    private static final String INSERT_COMMENT =
            "INSERT INTO pull_request_comment ("
                    + " c_url,"
                    + " pr_url,"
                    + " author,"
                    + " content,"
                    + " is_deleted,"
                    + " created_ts)"
                    + " SELECT"
                    + " md5(random()::text),"
                    + " p.pr_url,"
                    + " md5(random()::text),"
                    + " md5(random()::text),"
                    + " false,"
                    + " p.created_ts + CAST(:comment_ts_offset AS INTERVAL)"
                    + " FROM pull_request AS p";

    @Test
    void reports_cycle_time() {
        jdbi.useHandle(new RollbackTransaction(this::report_cycle_time));
    }

    @Test
    void reports_ttfa() {
        jdbi.useHandle(new RollbackTransaction(this::report_ttfa));
    }

    @Test
    void reports_ttla() {
        jdbi.useHandle(new RollbackTransaction(this::report_ttla));
    }

    @Test
    void reports_ttfc() {
        jdbi.useHandle(new RollbackTransaction(this::report_ttfc));
    }

    @Test
    void reports_ttlc() {
        jdbi.useHandle(new RollbackTransaction(this::report_ttlc));
    }

    @Test
    void reports_ttff() {
        jdbi.useHandle(new RollbackTransaction(this::report_ttff));
    }

    private void report_cycle_time(final Handle handle) {

        resetDatabase(handle);

        final var someOpenStart = Instant.parse("2017-10-25T21:02:17Z");
        final var someMergedStart = Instant.parse("2018-10-25T21:02:17Z");
        final var someMergedEnd = Instant.parse("2019-07-20T03:59:35Z");
        final var someDeclinedStart = Instant.parse("2018-10-25T22:02:17Z");
        final var someDeclinedEnd = Instant.parse("2020-01-20T03:59:35Z");

        final var batch = handle.prepareBatch(INSERT_PR);
        batch.bind("state", "OPEN");
        batch.bind("created_ts", someOpenStart);
        batch.bind("closed_ts", (Instant) null);
        batch.add();
        batch.bind("state", "MERGED");
        batch.bind("created_ts", someMergedStart);
        batch.bind("closed_ts", someMergedEnd);
        batch.add();
        batch.bind("state", "DECLINED");
        batch.bind("created_ts", someDeclinedStart);
        batch.bind("closed_ts", someDeclinedEnd);
        batch.add();

        batch.execute();

        final var actual = service.dataFor(DurationDensityReport.cycle_time);

        final var expected =
                List.of(
                        new StartEndRecord(
                                someMergedStart.toEpochMilli(),
                                someMergedEnd.toEpochMilli()),
                        new StartEndRecord(
                                someDeclinedStart.toEpochMilli(),
                                someDeclinedEnd.toEpochMilli()));

        assertThat(actual).hasSize(2).isEqualTo(expected);
    }

    private void report_ttfa(final Handle handle) {

        resetDatabase(handle);

        final var someOpenStart = Instant.parse("2017-10-25T21:02:17Z");
        final var someMergedStart = Instant.parse("2018-10-25T21:02:17Z");
        final var someMergedEnd = Instant.parse("2019-07-20T03:59:35Z");
        final var someDeclinedStart = Instant.parse("2018-10-25T22:02:17Z");
        final var someDeclinedEnd = Instant.parse("2020-01-20T03:59:35Z");

        final var batch = handle.prepareBatch(INSERT_PR);
        batch.bind("state", "OPEN");
        batch.bind("created_ts", someOpenStart);
        batch.bind("closed_ts", (Instant) null);
        batch.add();
        batch.bind("state", "MERGED");
        batch.bind("created_ts", someMergedStart);
        batch.bind("closed_ts", someMergedEnd);
        batch.add();
        batch.bind("state", "DECLINED");
        batch.bind("created_ts", someDeclinedStart);
        batch.bind("closed_ts", someDeclinedEnd);
        batch.add();

        batch.execute();

        handle.createUpdate(INSERT_APPROVAL)
                .bind("approval_ts_offset", "1h")
                .execute();

        final var actual = service.dataFor(DurationDensityReport.ttfa);

        final var expected =
                List.of(
                        new StartEndRecord(
                                someOpenStart.toEpochMilli(),
                                someOpenStart
                                        .plus(1, ChronoUnit.HOURS)
                                        .toEpochMilli()),
                        new StartEndRecord(
                                someMergedStart.toEpochMilli(),
                                someMergedStart
                                        .plus(1, ChronoUnit.HOURS)
                                        .toEpochMilli()),
                        new StartEndRecord(
                                someDeclinedStart.toEpochMilli(),
                                someDeclinedStart
                                        .plus(1, ChronoUnit.HOURS)
                                        .toEpochMilli()));

        assertThat(actual).isEqualTo(expected);
    }

    private void report_ttla(final Handle handle) {

        resetDatabase(handle);

        final var someOpenStart = Instant.parse("2017-10-25T21:02:17Z");

        final var batch = handle.prepareBatch(INSERT_PR);
        batch.bind("state", "OPEN");
        batch.bind("created_ts", someOpenStart);
        batch.bind("closed_ts", (Instant) null);
        batch.add();

        batch.execute();

        handle.createUpdate(INSERT_APPROVAL)
                .bind("approval_ts_offset", "1h")
                .execute();
        handle.createUpdate(INSERT_APPROVAL)
                .bind("approval_ts_offset", "2h")
                .execute();

        final var actual = service.dataFor(DurationDensityReport.ttla);

        final var expected =
                List.of(
                        new StartEndRecord(
                                someOpenStart.toEpochMilli(),
                                someOpenStart
                                        .plus(2, ChronoUnit.HOURS)
                                        .toEpochMilli()));

        assertThat(actual).isEqualTo(expected);
    }

    private void report_ttfc(final Handle handle) {

        resetDatabase(handle);

        final var someOpenStart = Instant.parse("2017-10-25T21:02:17Z");
        final var someMergedStart = Instant.parse("2018-10-25T21:02:17Z");
        final var someMergedEnd = Instant.parse("2019-07-20T03:59:35Z");
        final var someDeclinedStart = Instant.parse("2018-10-25T22:02:17Z");
        final var someDeclinedEnd = Instant.parse("2020-01-20T03:59:35Z");

        final var batch = handle.prepareBatch(INSERT_PR);
        batch.bind("state", "OPEN");
        batch.bind("created_ts", someOpenStart);
        batch.bind("closed_ts", (Instant) null);
        batch.add();
        batch.bind("state", "MERGED");
        batch.bind("created_ts", someMergedStart);
        batch.bind("closed_ts", someMergedEnd);
        batch.add();
        batch.bind("state", "DECLINED");
        batch.bind("created_ts", someDeclinedStart);
        batch.bind("closed_ts", someDeclinedEnd);
        batch.add();

        batch.execute();

        handle.createUpdate(INSERT_COMMENT)
                .bind("comment_ts_offset", "1h")
                .execute();

        final var actual = service.dataFor(DurationDensityReport.ttfc);

        final var expected =
                List.of(
                        new StartEndRecord(
                                someOpenStart.toEpochMilli(),
                                someOpenStart
                                        .plus(1, ChronoUnit.HOURS)
                                        .toEpochMilli()),
                        new StartEndRecord(
                                someMergedStart.toEpochMilli(),
                                someMergedStart
                                        .plus(1, ChronoUnit.HOURS)
                                        .toEpochMilli()),
                        new StartEndRecord(
                                someDeclinedStart.toEpochMilli(),
                                someDeclinedStart
                                        .plus(1, ChronoUnit.HOURS)
                                        .toEpochMilli()));

        assertThat(actual).isEqualTo(expected);
    }

    private void report_ttlc(final Handle handle) {

        resetDatabase(handle);

        final var someOpenStart = Instant.parse("2017-10-25T21:02:17Z");

        final var batch = handle.prepareBatch(INSERT_PR);
        batch.bind("state", "OPEN");
        batch.bind("created_ts", someOpenStart);
        batch.bind("closed_ts", (Instant) null);
        batch.add();

        batch.execute();

        handle.createUpdate(INSERT_COMMENT)
                .bind("comment_ts_offset", "1h")
                .execute();
        handle.createUpdate(INSERT_COMMENT)
                .bind("comment_ts_offset", "2h")
                .execute();

        final var actual = service.dataFor(DurationDensityReport.ttlc);

        final var expected =
                List.of(
                        new StartEndRecord(
                                someOpenStart.toEpochMilli(),
                                someOpenStart
                                        .plus(2, ChronoUnit.HOURS)
                                        .toEpochMilli()));

        assertThat(actual).isEqualTo(expected);
    }

    private void report_ttff(final Handle handle) {

        resetDatabase(handle);

        final var someOpenStart = Instant.parse("2017-10-25T21:02:17Z");

        final var batch = handle.prepareBatch(INSERT_PR);
        batch.bind("state", "OPEN");
        batch.bind("created_ts", someOpenStart);
        batch.bind("closed_ts", (Instant) null);
        batch.add();

        batch.execute();

        handle.createUpdate(INSERT_COMMENT)
                .bind("comment_ts_offset", "1h")
                .execute();
        handle.createUpdate(INSERT_APPROVAL)
                .bind("approval_ts_offset", "2h")
                .execute();

        final var actual = service.dataFor(DurationDensityReport.ttlc);

        final var expected =
                List.of(
                        new StartEndRecord(
                                someOpenStart.toEpochMilli(),
                                someOpenStart
                                        .plus(1, ChronoUnit.HOURS)
                                        .toEpochMilli()));

        assertThat(actual).isEqualTo(expected);
    }

    private static void resetDatabase(final Handle handle) {
        handle.createUpdate("DELETE FROM pull_request_approval").execute();
        handle.createUpdate("DELETE FROM pull_request_comment").execute();
        handle.createUpdate("DELETE FROM pull_request").execute();
    }
}
