package io.gitlab.mkjeldsen.prstatbucket;

import com.github.scribejava.core.builder.api.DefaultApi20;

public final class BitbucketApi extends DefaultApi20 {
    @Override
    public String getAccessTokenEndpoint() {
        return "https://bitbucket.org/site/oauth2/access_token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://bitbucket.org/site/oauth2/authorize";
    }
}
