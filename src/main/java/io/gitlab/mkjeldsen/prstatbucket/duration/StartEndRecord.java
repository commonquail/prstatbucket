package io.gitlab.mkjeldsen.prstatbucket.duration;

import java.util.Objects;

public final class StartEndRecord {
    public final long start;
    public final long end;

    public StartEndRecord(final long start, final long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "StartEndRecord{" + "start=" + start + ", end=" + end + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var other = (StartEndRecord) o;
        return start == other.start && end == other.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
