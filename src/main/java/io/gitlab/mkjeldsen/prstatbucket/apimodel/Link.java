package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Link {

    public final String href;

    /** @param href */
    public Link(@JsonProperty("href") String href) {
        this.href = href;
    }
}
