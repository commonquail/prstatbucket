package io.gitlab.mkjeldsen.prstatbucket.testhelper;

import io.gitlab.mkjeldsen.prstatbucket.JsonSupplier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public final class TestResourceJsonSupplier implements JsonSupplier {

    private final Function<String, String> urlOverride;

    public TestResourceJsonSupplier(
            final Function<String, String> urlOverride) {
        this.urlOverride = urlOverride;
    }

    @Override
    public String getJson(final String url) throws IOException {
        final var resourcePath = urlOverride.apply(url);
        try (var json = getClass().getResourceAsStream(resourcePath)) {
            assert json != null : resourcePath;
            byte[] bytes = json.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    @Override
    public void close() {}
}
