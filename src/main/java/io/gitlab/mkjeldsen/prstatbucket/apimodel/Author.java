package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public final class Author {

    public final String username;
    public final String displayName;
    public final String accountId;
    public final ProfileLinks links;
    public final String nickname;
    public final String type;
    public final UUID uuid;

    /**
     * @param username
     * @param accountId
     * @param nickname
     * @param links
     * @param wrappedUuid
     * @param type
     * @param displayName
     */
    public Author(
            @JsonProperty("username") String username,
            @JsonProperty("display_name") String displayName,
            @JsonProperty("account_id") String accountId,
            @JsonProperty("links") ProfileLinks links,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("type") String type,
            @JsonProperty("uuid") String wrappedUuid) {
        this.username = username;
        this.displayName = displayName;
        this.accountId = accountId;
        this.links = links;
        this.nickname = nickname;
        this.type = type;
        this.uuid =
                UUID.fromString(
                        wrappedUuid.substring(1, wrappedUuid.length() - 1));
    }
}
