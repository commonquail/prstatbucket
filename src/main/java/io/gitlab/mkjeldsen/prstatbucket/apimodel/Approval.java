package io.gitlab.mkjeldsen.prstatbucket.apimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Approval {

    public final Instant date;

    public final User approver;

    public Approval(Instant date, User approver) {
        this.date = date;
        this.approver = approver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Approval approval = (Approval) o;
        return date.equals(approval.date) && approver.equals(approval.approver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, approver);
    }

    @Override
    public String toString() {
        return "Approval{"
                + "date="
                + date
                + ", approver='"
                + approver
                + '\''
                + '}';
    }
}
