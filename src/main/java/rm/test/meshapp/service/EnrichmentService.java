package rm.test.meshapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rm.test.meshapp.entity.Account;
import rm.test.meshapp.repository.AccountRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrichmentService {

    private static final BigDecimal RATE = new BigDecimal("1.10");
    private static final BigDecimal MAX_MULTIPLIER = new BigDecimal("2.07");
    private static final int SCALE = 2;

    private final AccountRepository accountRepository;

    public List<Long> findAllAccountIds() {
        return accountRepository.findAllIds();
    }

    /**
     * Accrues 10% enrichment on a single account under a pessimistic row lock.
     * Caps balance at 207% of initial_balance. Returns true if the balance changed.
     */
    @Transactional
    public boolean accrueEnrichment(Long accountId) {
        Account account = accountRepository.findByIdForUpdate(accountId).orElse(null);
        if (account == null) {
            return false;
        }

        BigDecimal max = account.getInitialBalance()
                .multiply(MAX_MULTIPLIER)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal balance = account.getBalance();
        if (balance.compareTo(max) >= 0) {
            return false;
        }

        BigDecimal candidate = balance.multiply(RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal newBalance = candidate.min(max);

        account.setBalance(newBalance);
        log.debug(
                "Enrichment accountId={} balance {}->{} (max={})",
                accountId, balance, newBalance, max
        );
        return true;
    }
}
