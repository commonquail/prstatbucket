package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Repository {

    public final ProfileLinks links;
    public final String type;
    public final String name;
    public final String fullName;
    public final UUID uuid;

    /**
     * @param name
     * @param links
     * @param wrappedUuid
     * @param fullName
     * @param type
     */
    public Repository(
            @JsonProperty("links") ProfileLinks links,
            @JsonProperty("type") String type,
            @JsonProperty("name") String name,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("uuid") String wrappedUuid) {
        this.links = links;
        this.type = type;
        this.name = name;
        this.fullName = fullName;
        this.uuid =
                UUID.fromString(
                        wrappedUuid.substring(1, wrappedUuid.length() - 1));
    }
}
