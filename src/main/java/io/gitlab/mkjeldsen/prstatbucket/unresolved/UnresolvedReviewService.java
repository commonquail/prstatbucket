package io.gitlab.mkjeldsen.prstatbucket.unresolved;

import java.util.List;

public interface UnresolvedReviewService {
    List<UnresolvedReview> getOpenPullRequests();
}
