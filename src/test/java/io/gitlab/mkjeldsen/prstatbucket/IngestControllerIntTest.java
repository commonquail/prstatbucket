package io.gitlab.mkjeldsen.prstatbucket;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(IngestController.class)
@EnabledIf("${test.database:false}")
final class IngestControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @Qualifier("ingester")
    private Ingester ingester;

    @MockBean
    @Qualifier("unresolvedPrIngester")
    private Ingester unresolvedPrIngester;

    @Test
    void ingest_serves_json() throws Exception {
        Mockito.when(ingester.isBusy()).thenReturn(false);

        final var requestDefault =
                post("/ingest").contentType(MediaType.APPLICATION_JSON);
        final var matchingMediaType =
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);
        final var matchingSubstring =
                content()
                        .string(
                                allOf(
                                        containsString("\"start_time\""),
                                        containsString("\"id\""),
                                        containsString("\"urls\"")));

        this.mockMvc
                .perform(requestDefault)
                .andExpect(status().isAccepted())
                .andExpect(matchingMediaType)
                .andExpect(matchingSubstring);
    }

    @Test
    void ingest_unresolved_serves_json() throws Exception {
        Mockito.when(unresolvedPrIngester.isBusy()).thenReturn(false);

        final var requestDefault =
                post("/ingest-unresolved")
                        .contentType(MediaType.APPLICATION_JSON);
        final var matchingMediaType =
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);
        final var matchingSubstring =
                content()
                        .string(
                                allOf(
                                        containsString("\"start_time\""),
                                        containsString("\"id\""),
                                        containsString("\"urls\"")));

        this.mockMvc
                .perform(requestDefault)
                .andExpect(status().isAccepted())
                .andExpect(matchingMediaType)
                .andExpect(matchingSubstring);
    }
}
