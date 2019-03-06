package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class MergeCommit {

    public final String hash;
    public final String type;
    public final CommitLinks links;

    /**
     * @param hash
     * @param type
     * @param links
     */
    public MergeCommit(
            @JsonProperty("hash") String hash,
            @JsonProperty("type") String type,
            @JsonProperty("links") CommitLinks links) {
        this.hash = hash;
        this.type = type;
        this.links = links;
    }
}
