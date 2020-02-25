package io.gitlab.mkjeldsen.prstatbucket.duration;

import com.github.benmanes.caffeine.cache.Cache;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/duration")
public class DurationDensityEstimateController {

    private final Clock clock;

    private final DurationDensityEstimateService service;

    private final Cache<DurationDensityReport, Long> lastModifiedCache;

    public DurationDensityEstimateController(
            final Clock clock,
            @Qualifier("cachingDurationDensityEstimateService")
                    final DurationDensityEstimateService service,
            final Cache<DurationDensityReport, Long> lastModifiedCache) {
        this.clock = clock;
        this.service = service;
        this.lastModifiedCache = lastModifiedCache;
    }

    @GetMapping("/density")
    public ModelAndView asHtml() {
        return new ModelAndView("duration-density");
    }

    // CSV media types per RFC 4180.
    //
    // At 100k records, JSON payload beats CSV on parse-time with tens of ms but
    // takes several seconds longer to download even at 10 Mbit/s.
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/density/{report}.csv",
            consumes = "text/csv",
            produces = "text/csv")
    public void asCsv(
            final WebRequest request,
            final HttpServletResponse response,
            @PathVariable("report") final String reportName)
            throws IOException {

        response.setContentType("text/csv; charset=utf-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        final DurationDensityReport report;
        try {
            report = DurationDensityReport.valueOf(reportName);
        } catch (IllegalArgumentException iae) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        final Long lastModified =
                lastModifiedCache.get(
                        report, ignore -> Instant.now(clock).toEpochMilli());
        assert lastModified != null : "currentTimeMillis() returned null";
        if (request.checkNotModified(lastModified)) {
            return;
        }

        // Don't bother streaming. 1 MB fits about 60k records of 2 x long: way
        // more than anyone is likely to need and without the maintenance and
        // runtime overhead of Spring async handlers. This also makes it trivial
        // to cache the response.
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=\"" + report + ".csv\"");

        final var records = service.dataFor(report);

        // <epoch> "," <epoch> LF => <13> "," <13> LF => 14 * 2
        final int lengthOfRecord = 14 * 2;
        // 1 header + n records.
        final int lineCount = 1 + records.size();
        final var payload = new StringBuilder(lengthOfRecord * lineCount);
        payload.append("start,end\n");
        for (final var record : records) {
            payload.append(record.start)
                    .append(',')
                    .append(record.end)
                    .append('\n'); // Ensure LF
        }

        response.setHeader(HttpHeaders.CONTENT_LENGTH, "" + payload.length());

        response.getWriter().write(payload.toString());
    }
}
