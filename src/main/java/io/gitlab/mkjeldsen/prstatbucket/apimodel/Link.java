package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Link {

    public final String href;

    /** @param href */
    public Link(@JsonProperty("href") String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        return "Link{" + "href='" + href + '\'' + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Link link = (Link) o;
        return href.equals(link.href);
    }

    @Override
    public int hashCode() {
        return Objects.hash(href);
    }
}
