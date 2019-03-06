package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ProfileLinks {

    public final Link self;
    public final Link html;
    public final Link avatar;

    /**
     * @param html
     * @param self
     * @param avatar
     */
    public ProfileLinks(
            @JsonProperty("self") Link self,
            @JsonProperty("html") Link html,
            @JsonProperty("avatar") Link avatar) {
        super();
        this.self = self;
        this.html = html;
        this.avatar = avatar;
    }
}
