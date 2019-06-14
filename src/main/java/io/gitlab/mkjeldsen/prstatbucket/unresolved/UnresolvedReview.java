package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import java.time.Duration;
import java.util.Objects;

public final class UnresolvedReview implements Comparable<UnresolvedReview> {

    private final String url;

    private final String destination;

    private final String title;

    private final Duration rawAge;

    private final String age;

    public UnresolvedReview(
            String url,
            String destination,
            String title,
            Duration rawAge,
            String age) {
        this.url = url;
        this.destination = Objects.requireNonNull(destination);
        this.title = Objects.requireNonNull(title);
        this.rawAge = Objects.requireNonNull(rawAge);
        this.age = Objects.requireNonNull(age);
    }

    public String getTitle() {
        return title;
    }

    public String getAge() {
        return age;
    }

    public String getDestination() {
        return destination;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "UnresolvedReview{"
                + "url='"
                + url
                + '\''
                + ", destination='"
                + destination
                + '\''
                + ", title='"
                + title
                + '\''
                + ", age='"
                + age
                + '\''
                + '}';
    }

    @Override
    public int compareTo(UnresolvedReview other) {
        // Older pull requests are higher priority than newer pull requests but
        // shorter durations are "less than" longer durations, so invert order.
        int res = rawAge.compareTo(other.rawAge) * -1;
        if (res == 0) {
            res = title.compareTo(other.title);
        }
        return res;
    }
}