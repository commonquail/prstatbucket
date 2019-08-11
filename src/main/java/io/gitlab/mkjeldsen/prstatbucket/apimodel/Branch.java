package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Branch {

    public final String name;

    /** @param name */
    public Branch(@JsonProperty("name") String name) {
        this.name = name;
    }
}
