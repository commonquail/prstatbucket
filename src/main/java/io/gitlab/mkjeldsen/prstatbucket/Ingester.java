package io.gitlab.mkjeldsen.prstatbucket;

import java.util.Collection;

/**
 * An ingester orchestrates the retrieval, deserialization, and persistence of
 * resources.
 */
public interface Ingester {

    /**
     * Ingests every provided URL.
     *
     * @param urls a collection of well-formed URLs
     * @see #ingest(String)
     */
    void ingestAll(Collection<String> urls);

    /**
     * Ingests a single well-formed URL
     *
     * @param url a single well-formed URL
     * @see #ingestAll(Collection)
     */
    void ingest(String url);

    /** True if this ingester is working. */
    boolean isBusy();
}
