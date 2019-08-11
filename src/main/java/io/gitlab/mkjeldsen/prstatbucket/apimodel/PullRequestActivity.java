package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class PullRequestActivity {

    public final String url;
    private final String next;
    public final Instant closedTs;
    public final List<Comment> comments;
    public final int approvals;

    public PullRequestActivity(
            String prUrl,
            String next,
            Instant closedTs,
            List<Comment> comments,
            int approvals) {
        this.url = prUrl;
        this.next = next;
        this.closedTs = closedTs;
        this.comments = List.copyOf(comments);
        this.approvals = approvals;
    }

    public Optional<String> getNext() {
        return Optional.ofNullable(next);
    }

    public Optional<String> getPrevious() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "PullRequestActivity{"
                + "url='"
                + url
                + '\''
                + ", next='"
                + next
                + '\''
                + ", closedTs="
                + closedTs
                + ", comments=["
                + comments.size()
                + "], approvals="
                + approvals
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PullRequestActivity that = (PullRequestActivity) o;
        return approvals == that.approvals
                && url.equals(that.url)
                && Objects.equals(next, that.next)
                && closedTs == that.closedTs
                && comments.equals(that.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, next, closedTs, comments, approvals);
    }
}
