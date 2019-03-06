package io.gitlab.mkjeldsen.prstatbucket;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClientConfig;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LiveJsonSupplier implements JsonSupplier {

    private static final Logger LOG =
            LoggerFactory.getLogger(LiveJsonSupplier.class);

    private final OAuth20Service service;

    private final CompletableFuture<OAuth2AccessToken> grant;

    public LiveJsonSupplier(String clientId, String secret) {
        service =
                new ServiceBuilder(clientId)
                        .apiSecret(secret)
                        .httpClientConfig(
                                OkHttpHttpClientConfig.defaultConfig())
                        .build(new BitbucketApi());

        grant = CompletableFuture.supplyAsync(this::init);
    }

    private OAuth2AccessToken init() {
        try {
            return service.getAccessTokenClientCredentialsGrant();
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getJson(String url) throws IOException {
        LOG.info("Retrieve '{}'", url);

        try {
            final var request = new OAuthRequest(Verb.GET, url);
            service.signRequest(grant.get(), request);
            final Response response = service.execute(request);

            return response.getBody();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        service.close();
    }
}
