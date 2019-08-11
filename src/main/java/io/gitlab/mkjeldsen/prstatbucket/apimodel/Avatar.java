package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"href"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Avatar {

    @JsonProperty("href")
    public String href;

    /** No args constructor for use in serialization */
    public Avatar() {}

    /** @param href */
    public Avatar(String href) {
        super();
        this.href = href;
    }
}
