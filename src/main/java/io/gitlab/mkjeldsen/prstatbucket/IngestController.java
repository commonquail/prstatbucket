package io.gitlab.mkjeldsen.prstatbucket;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class IngestController {

    private final Collection<String> urls;

    private final Ingester ingester;

    private final Ingester unresolvedPrIngester;

    private HashMap<String, Object> lastInfo;

    private HashMap<String, Object> lastInfoUnresolved;

    public IngestController(
            @Qualifier("ingester") Ingester ingester,
            @Qualifier("unresolvedPrIngester") Ingester unresolvedPrIngester,
            Collection<String> repositoryUrls) {
        this.ingester = ingester;
        this.unresolvedPrIngester = unresolvedPrIngester;
        this.urls = repositoryUrls;
        lastInfo = new HashMap<>();
        lastInfoUnresolved = new HashMap<>();
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest() {
        synchronized (ingester) {
            if (!ingester.isBusy()) {
                var info = new HashMap<String, Object>();
                info.put("id", UUID.randomUUID());
                info.put("start_time", Instant.now());
                info.put("urls", urls);
                this.lastInfo = info;
                ingester.ingestAll(urls);
            }
            return ResponseEntity.accepted().body(this.lastInfo);
        }
    }

    @PostMapping("/ingest-unresolved")
    public ResponseEntity<Map<String, Object>> ingestUnresolved() {
        synchronized (unresolvedPrIngester) {
            if (!unresolvedPrIngester.isBusy()) {
                var info = new HashMap<String, Object>();
                info.put("id", UUID.randomUUID());
                info.put("start_time", Instant.now());
                info.put("urls", urls);
                this.lastInfoUnresolved = info;
                unresolvedPrIngester.ingestAll(urls);
            }
            return ResponseEntity.accepted().body(this.lastInfoUnresolved);
        }
    }
}
