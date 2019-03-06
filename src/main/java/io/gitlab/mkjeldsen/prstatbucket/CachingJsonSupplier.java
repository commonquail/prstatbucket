package io.gitlab.mkjeldsen.prstatbucket;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CachingJsonSupplier implements JsonSupplier {

    private static final Logger LOG =
            LoggerFactory.getLogger(CachingJsonSupplier.class);

    // pullrequests/activity arbitrarily uses "ctx" for pagination.
    private static final Pattern QUERY_PARAM_PAGE =
            Pattern.compile("(?:page|ctx)=(\\w+)");

    private final JsonSupplier delegate;

    private final Path cacheDir;

    public CachingJsonSupplier(Path cachePath, JsonSupplier delegate) {
        this.delegate = delegate;

        try {
            cacheDir = Files.createDirectories(cachePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractPageNumber(URI uri) {
        final var query = uri.getQuery();

        if (query != null) {
            final var matcher = QUERY_PARAM_PAGE.matcher(query);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return "1";
    }

    @Override
    public String getJson(String url) throws IOException {
        final var uri = URI.create(url);

        final var file = cacheDir.resolve(uriToFilePath(uri));

        try {
            LOG.info("Play back '{}'", file);

            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            LOG.info("Record '{}'", file);

            Files.createDirectories(file.getParent());

            final var body = delegate.getJson(url);

            Files.writeString(file, body);

            return body;
        }
    }

    private static String uriToFilePath(URI uri) {
        return uri.getHost()
                + uri.getPath()
                + "/p"
                + extractPageNumber(uri)
                + ".json";
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }
}
