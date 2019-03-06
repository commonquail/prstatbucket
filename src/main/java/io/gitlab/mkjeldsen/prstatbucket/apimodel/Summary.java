package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Summary {

    public final String raw;
    public final String markup;
    public final String html;
    public final String type;

    /**
     * @param raw
     * @param markup
     * @param html
     * @param type
     */
    public Summary(
            @JsonProperty("raw") String raw,
            @JsonProperty("markup") String markup,
            @JsonProperty("html") String html,
            @JsonProperty("type") String type) {
        this.raw = raw;
        this.markup = markup;
        this.html = html;
        this.type = type;
    }
}
