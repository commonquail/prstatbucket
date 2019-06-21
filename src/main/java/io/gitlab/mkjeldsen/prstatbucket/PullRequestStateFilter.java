package io.gitlab.mkjeldsen.prstatbucket;

import io.gitlab.mkjeldsen.prstatbucket.apimodel.PullRequest;
import java.util.EnumSet;
import java.util.function.Predicate;

public final class PullRequestStateFilter implements Predicate<PullRequest> {

    private final EnumSet<PullRequest.State> states;

    public PullRequestStateFilter(EnumSet<PullRequest.State> states) {
        this.states = EnumSet.copyOf(states);
    }

    public static PullRequestStateFilter all() {
        return new PullRequestStateFilter(
                EnumSet.allOf(PullRequest.State.class));
    }

    public static PullRequestStateFilter open() {
        return new PullRequestStateFilter(EnumSet.of(PullRequest.State.OPEN));
    }

    @Override
    public boolean test(PullRequest pr) {
        return states.contains(pr.state);
    }
}
