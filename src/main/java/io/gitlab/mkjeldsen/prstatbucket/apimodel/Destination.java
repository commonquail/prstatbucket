package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Destination {

    public final Commit commit;
    public final Repository repository;
    public final Branch branch;

    /**
     * @param repository
     * @param commit
     * @param branch
     */
    public Destination(
            @JsonProperty("commit") Commit commit,
            @JsonProperty("repository") Repository repository,
            @JsonProperty("branch") Branch branch) {
        this.commit = commit;
        this.repository = repository;
        this.branch = branch;
    }
}
