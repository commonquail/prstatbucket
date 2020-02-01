package io.gitlab.mkjeldsen.prstatbucket;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.Comment;
import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequestActivity;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeserializePullRequestActivity
        extends JsonDeserializer<PullRequestActivity> {

    private static final Logger LOG =
            LoggerFactory.getLogger(DeserializePullRequestActivity.class);

    public DeserializePullRequestActivity() {}

    @Override
    public PullRequestActivity deserialize(
            JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        String prUrl = null;
        String nextUrl = null;
        Instant prClosedTs = null;
        final var comments = new ArrayList<Comment>();
        int approvals = 0;

        JsonNode next = node.get("next");
        if (next != null) {
            nextUrl = next.asText();
        }

        JsonNode values = node.get("values");
        for (var v : values) {
            if (prUrl == null) {
                prUrl =
                        v.get("pull_request")
                                .get("links")
                                .get("html")
                                .get("href")
                                .asText();
            }

            JsonNode update = v.get("update");
            if (update != null) {
                var state = update.get("state").asText();
                if (!state.equals("OPEN")) {
                    String date = update.get("date").asText();
                    Instant ts = ZonedDateTime.parse(date).toInstant();
                    if (prClosedTs == null || ts.isBefore(prClosedTs)) {
                        prClosedTs = ts;
                    }
                }
            }

            JsonNode comment = v.get("comment");
            if (comment != null) {
                boolean deleted = comment.get("deleted").asBoolean();

                var url = comment.get("links").get("html").get("href").asText();
                var content = comment.get("content").get("raw").asText();

                var author = getUser(comment);

                Instant ts =
                        ZonedDateTime.parse(comment.get("created_on").asText())
                                .toInstant();

                comments.add(new Comment(url, content, ts, deleted, author));
            }

            JsonNode approval = v.get("approval");
            if (approval != null) {
                ++approvals;
            }

            if (update == null && comment == null && approval == null) {
                LOG.warn("Unrecognized activity: {}", v);
            }
        }

        return new PullRequestActivity(
                prUrl, nextUrl, prClosedTs, comments, approvals);
    }

    private String getUser(final JsonNode node) {
        // Use UUID. Lots of users don't have an Atlassian "account_id" value
        // yet.
        // https://developer.atlassian.com/cloud/bitbucket/bbc-gdpr-api-migration-guide/
        var user = node.path("user").path("uuid").asText("<deleted>");
        if (user.charAt(0) == '{') {
            return user.substring(1, user.length() - 1);
        }
        return user;
    }
}
