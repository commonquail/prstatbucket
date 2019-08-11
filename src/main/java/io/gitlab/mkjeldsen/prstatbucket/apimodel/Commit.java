package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Commit {

    public final String hash;
    public final String type;
    public final CommitLinks links;

    /**
     * @param hash
     * @param links
     * @param type
     */
    public Commit(
            @JsonProperty("hash") String hash,
            @JsonProperty("type") String type,
            @JsonProperty("links") CommitLinks links) {
        this.hash = hash;
        this.type = type;
        this.links = links;
    }
}
