package io.gitlab.mkjeldsen.prstatbucket;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class IngestController {

    private final Collection<String> urls;

    private final Ingester ingester;

    private HashMap<String, Object> lastInfo;

    public IngestController(
            Ingester ingester, Collection<String> repositoryUrls) {
        this.ingester = ingester;
        this.urls = repositoryUrls;
        lastInfo = new HashMap<>();
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
}
