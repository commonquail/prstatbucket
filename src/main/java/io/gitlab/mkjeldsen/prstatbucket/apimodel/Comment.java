package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import java.time.Instant;
import java.util.Objects;

public final class Comment {

    public final String url;

    public final String content;

    public final Instant createdOn;

    public final boolean deleted;

    public final String author;

    public Comment(
            String url,
            String content,
            Instant createdOn,
            boolean deleted,
            String author) {
        this.url = url;
        this.content = content;
        this.createdOn = createdOn;
        this.deleted = deleted;
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return deleted == comment.deleted
                && url.equals(comment.url)
                && content.equals(comment.content)
                && createdOn.equals(comment.createdOn)
                && author.equals(comment.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, content, createdOn, deleted, author);
    }

    @Override
    public String toString() {
        return "Comment{"
                + "url='"
                + url
                + '\''
                + ", content='"
                + content
                + '\''
                + ", createdOn="
                + createdOn
                + ", deleted="
                + deleted
                + ", author='"
                + author
                + '\''
                + '}';
    }
}
