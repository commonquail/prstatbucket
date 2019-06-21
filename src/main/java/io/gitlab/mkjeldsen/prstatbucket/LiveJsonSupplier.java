package io.gitlab.mkjeldsen.prstatbucket;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClientConfig;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LiveJsonSupplier implements JsonSupplier {

    private static final Logger LOG =
            LoggerFactory.getLogger(LiveJsonSupplier.class);

    private final OAuth20Service service;

    private volatile OAuth2AccessToken grant;

    private volatile long then;

    public LiveJsonSupplier(String clientId, String secret) {
        service =
                new ServiceBuilder(clientId)
                        .apiSecret(secret)
                        .httpClientConfig(
                                OkHttpHttpClientConfig.defaultConfig())
                        .build(new BitbucketApi());

        then = 0;
        grant = new OAuth2AccessToken("expired-dummy");
    }

    @Override
    public String getJson(String url) throws IOException {
        LOG.info("Retrieve '{}'", url);

        try {
            ensureGrant();

            final var request = new OAuthRequest(Verb.GET, url);
            service.signRequest(grant, request);
            final Response response = service.execute(request);

            return response.getBody();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void ensureGrant()
            throws InterruptedException, ExecutionException, IOException {
        final Integer expiresIn = grant.getExpiresIn();
        final int window = Math.max(expiresIn == null ? 0 : expiresIn - 20, 0);

        long now = System.currentTimeMillis() / 1000;
        if (then <= now - window) {
            grant = service.getAccessTokenClientCredentialsGrant();
            then = now;
        }
    }

    @Override
    public void close() throws Exception {
        service.close();
    }
}
