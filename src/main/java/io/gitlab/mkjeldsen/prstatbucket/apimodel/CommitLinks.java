package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class CommitLinks {

    public final Link self;
    public final Link html;

    /**
     * @param html
     * @param self
     */
    public CommitLinks(
            @JsonProperty("self") Link self, @JsonProperty("html") Link html) {
        this.self = self;
        this.html = html;
    }
}
