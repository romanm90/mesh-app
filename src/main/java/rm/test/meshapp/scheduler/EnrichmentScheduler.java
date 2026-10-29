package rm.test.meshapp.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rm.test.meshapp.service.EnrichmentService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.enrichment-scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class EnrichmentScheduler {

    private final EnrichmentService enrichmentService;

    @Scheduled(fixedRate = 30_000)
    public void accrueEnrichment() {
        List<Long> accountIds = enrichmentService.findAllAccountIds();
        int updated = 0;

        for (Long accountId : accountIds) {
            if (enrichmentService.accrueEnrichment(accountId)) {
                updated++;
            }
        }

        log.info("Enrichment accrual finished: updated {} of {} accounts", updated, accountIds.size());
    }
}
