package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class User {

    public static final User DELETED =
            new User(
                    "<deleted>",
                    "<deleted>",
                    "<deleted>",
                    new ProfileLinks(
                            new Link("https://deleted.example"),
                            new Link("https://deleted.example"),
                            new Link("https://deleted.example")),
                    "<deleted>",
                    "<deleted>",
                    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

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
    public User(
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

    @Override
    public String toString() {
        return "User{"
                + "username='"
                + username
                + '\''
                + ", displayName='"
                + displayName
                + '\''
                + ", accountId='"
                + accountId
                + '\''
                + ", links="
                + links
                + ", nickname='"
                + nickname
                + '\''
                + ", type='"
                + type
                + '\''
                + ", uuid="
                + uuid
                + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final User user = (User) o;
        return uuid.equals(user.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
