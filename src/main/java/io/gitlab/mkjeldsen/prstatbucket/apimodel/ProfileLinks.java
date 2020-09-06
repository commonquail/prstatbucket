package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    @Override
    public String toString() {
        return "ProfileLinks{"
                + "self="
                + self
                + ", html="
                + html
                + ", avatar="
                + avatar
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
        final ProfileLinks links = (ProfileLinks) o;
        return self.equals(links.self)
                && html.equals(links.html)
                && avatar.equals(links.avatar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, html, avatar);
    }
}
