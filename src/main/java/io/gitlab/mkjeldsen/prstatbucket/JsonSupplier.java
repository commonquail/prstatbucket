package io.gitlab.mkjeldsen.prstatbucket;

import java.io.IOException;

public interface JsonSupplier extends AutoCloseable {
    String getJson(String url) throws IOException;
}
