package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UnresolvedReviewController.class)
@EnabledIf("${smoke.tests.enabled:false}")
final class UnresolvedReviewControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UnresolvedReviewService unresolvedReviewService;

    @Test
    void serves_html_by_default() throws Exception {
        final var someData =
                List.of(
                        new UnresolvedReview(
                                "https://pr.example",
                                "foo-dest",
                                "foo",
                                Duration.ofHours(2),
                                "2h"));

        Mockito.when(unresolvedReviewService.getOpenPullRequests())
                .thenReturn(someData);

        final var requestDefault = get("/unresolved");
        final var matchingMediaType =
                content().contentTypeCompatibleWith(MediaType.TEXT_HTML);
        final var matchingSubstring =
                content().string(containsString("<td>2h</td>"));

        this.mockMvc
                .perform(requestDefault)
                .andExpect(status().isOk())
                .andExpect(matchingMediaType)
                .andExpect(matchingSubstring);
    }

    @Test
    void serves_json_on_request() throws Exception {
        final var someData =
                List.of(
                        new UnresolvedReview(
                                "https://pr2.example",
                                "bar-dest",
                                "bar",
                                Duration.ofDays(1)
                                        .plusHours(3)
                                        .plusMinutes(12)
                                        .plusSeconds(42),
                                "1d 3h 12m"));

        Mockito.when(unresolvedReviewService.getOpenPullRequests())
                .thenReturn(someData);

        final var requestJson =
                get("/unresolved").contentType(MediaType.APPLICATION_JSON_UTF8);
        final var matchingJsonBody =
                content().json(read("/unresolved-review.json"));
        final var matchingMediaType =
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);

        this.mockMvc
                .perform(requestJson)
                .andExpect(status().isOk())
                .andExpect(matchingMediaType)
                .andExpect(matchingJsonBody);
    }

    private String read(String file) throws IOException {
        try (var json = getClass().getResourceAsStream(file)) {
            byte[] bytes = json.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
