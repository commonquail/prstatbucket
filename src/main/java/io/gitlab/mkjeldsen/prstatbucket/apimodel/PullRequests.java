package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

public final class PullRequests {

    public final int pagelen;
    public final List<PullRequest> values;
    public final int page;
    public final int size;
    private final String next;
    private final String previous;

    /**
     * @param pagelen
     * @param values
     * @param page
     * @param size
     * @param next
     * @param previous
     */
    public PullRequests(
            @JsonProperty("pagelen") int pagelen,
            @JsonProperty("values") List<PullRequest> values,
            @JsonProperty("page") int page,
            @JsonProperty("size") int size,
            @JsonProperty("next") String next,
            @JsonProperty("previous") String previous) {
        super();
        this.pagelen = pagelen;
        this.values = List.copyOf(values);
        this.page = page;
        this.size = size;
        this.next = next;
        this.previous = previous;
    }

    public Optional<String> getNext() {
        return Optional.ofNullable(next);
    }

    public Optional<String> getPrevious() {
        return Optional.ofNullable(previous);
    }
}
