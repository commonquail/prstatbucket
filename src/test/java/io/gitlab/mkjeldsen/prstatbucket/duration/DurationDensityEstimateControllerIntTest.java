package io.gitlab.mkjeldsen.prstatbucket.duration;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.gitlab.mkjeldsen.prstatbucket.AppConfig;
import io.gitlab.mkjeldsen.prstatbucket.testhelper.FalseExternalDatabaseTest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest({
    AppConfig.class,
    DurationDensityEstimateController.class,
})
@FalseExternalDatabaseTest
final class DurationDensityEstimateControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Clock clock;

    @MockBean
    @Qualifier("cachingDurationDensityEstimateService")
    private DurationDensityEstimateService service;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());
    }

    @Test
    void serves_html_by_default() throws Exception {
        final var requestDefault = get("/duration/density");

        final var matchingMediaType =
                content().contentTypeCompatibleWith(MediaType.TEXT_HTML);

        final var pageTitle =
                containsString("<title>Duration Density Estimates</title>");
        final var d3Dependency = containsString("d3.v5.min.js");
        final var bundleScript = containsString("\"/js/bundle-");
        final var stylesheet = containsString("\"/css/duration-density-");
        final var appScript = containsString("Main.durationDensity()");
        final var plotRegion = containsString("id=\"canvas\"");
        final var pageContents =
                allOf(
                        pageTitle,
                        d3Dependency,
                        bundleScript,
                        appScript,
                        stylesheet,
                        plotRegion);
        final var matchingSubstring = content().string(pageContents);

        this.mockMvc
                .perform(requestDefault)
                .andExpect(status().isOk())
                .andExpect(matchingMediaType)
                .andExpect(matchingSubstring);
    }

    @ParameterizedTest
    @EnumSource(DurationDensityReport.class)
    void serves_csv_on_request(final DurationDensityReport report)
            throws Exception {
        final var someData =
                List.of(
                        new StartEndRecord(3787, 44742),
                        new StartEndRecord(1581054703299L, 1582055704299L));

        when(service.dataFor(report)).thenReturn(someData);

        final var requestCsv =
                get("/duration/density/" + report + ".csv")
                        .contentType("text/csv");

        final var expectedContent =
                "start,end\n"
                        + "3787,44742\n"
                        + "1581054703299,1582055704299\n";
        final var matchingCsvBody = content().string(expectedContent);

        final var matchingMediaType =
                content().contentTypeCompatibleWith("text/csv");

        final var matchingAttachment =
                header().string(
                                HttpHeaders.CONTENT_DISPOSITION,
                                "attachment;filename=\"" + report + ".csv\"");

        final var matchingLength =
                header().string(
                                HttpHeaders.CONTENT_LENGTH,
                                "" + expectedContent.length());

        this.mockMvc
                .perform(requestCsv)
                .andExpect(status().isOk())
                .andExpect(matchingMediaType)
                .andExpect(matchingAttachment)
                .andExpect(matchingLength)
                .andExpect(matchingCsvBody);
    }

    @Test
    void caches_unmodified_response() throws Exception {

        final var someReport = DurationDensityReport.cycle_time;

        when(service.dataFor(someReport)).thenReturn(emptyList());

        final var path = "/duration/density/" + someReport + ".csv";

        final var uncachedRequest = get(path).contentType("text/csv");

        final var matchingMediaType =
                content().contentTypeCompatibleWith("text/csv");

        final var someLastModified = Instant.ofEpochSecond(10);
        final var matchingLastModified =
                header().dateValue(
                                HttpHeaders.LAST_MODIFIED,
                                someLastModified.toEpochMilli());

        when(clock.instant()).thenReturn(someLastModified);

        this.mockMvc
                .perform(uncachedRequest)
                .andExpect(status().isOk())
                .andExpect(matchingLastModified);

        final var cachedRequest =
                get(path)
                        .header(
                                HttpHeaders.IF_MODIFIED_SINCE,
                                someLastModified.toEpochMilli())
                        .contentType("text/csv");

        final var noAttachment =
                header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION);
        final var noLength = header().doesNotExist(HttpHeaders.CONTENT_LENGTH);
        final var noBody = content().string("");

        this.mockMvc
                .perform(cachedRequest)
                .andExpect(status().isNotModified())
                .andExpect(matchingMediaType)
                .andExpect(noAttachment)
                .andExpect(noLength)
                .andExpect(matchingLastModified)
                .andExpect(noBody);
    }

    @Test
    void unsupported_report_name_yields_404_not_found() throws Exception {

        // To ensure that specifically the report name triggers the 404.
        final var requestKnownCsv =
                get("/duration/density/" + DurationDensityReport.ttff + ".csv")
                        .contentType("text/csv");

        final var requestUnknownCsv =
                get("/duration/density/not-supported.csv")
                        .contentType("text/csv");

        this.mockMvc.perform(requestKnownCsv).andExpect(status().isOk());
        this.mockMvc
                .perform(requestUnknownCsv)
                .andExpect(status().isNotFound());
    }
}
