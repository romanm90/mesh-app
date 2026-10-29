package rm.test.meshapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rm.test.meshapp.dto.TransferResponse;
import rm.test.meshapp.entity.Account;
import rm.test.meshapp.exception.NotFoundException;
import rm.test.meshapp.exception.UnprocessableException;
import rm.test.meshapp.repository.AccountRepository;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;

    @Transactional
    public TransferResponse transfer(Long fromUserId, Long toUserId, BigDecimal value) {
        validateTransfer(fromUserId, toUserId, value);

        // Lock accounts in ascending user_id order to avoid deadlocks
        long firstUserId = Math.min(fromUserId, toUserId);
        long secondUserId = Math.max(fromUserId, toUserId);

        Account first = lockAccount(firstUserId);
        Account second = lockAccount(secondUserId);

        Account fromAccount = fromUserId.equals(firstUserId) ? first : second;
        Account toAccount = toUserId.equals(secondUserId) ? second : first;

        BigDecimal fromBefore = fromAccount.getBalance();
        BigDecimal toBefore = toAccount.getBalance();

        if (fromBefore.compareTo(value) < 0) {
            throw new UnprocessableException("Insufficient funds");
        }

        BigDecimal fromAfter = fromBefore.subtract(value);
        BigDecimal toAfter = toBefore.add(value);

        fromAccount.setBalance(fromAfter);
        toAccount.setBalance(toAfter);

        log.info(
                "Transfer from={} to={} value={} fromBalance {}->{} toBalance {}->{}",
                fromUserId, toUserId, value, fromBefore, fromAfter, toBefore, toAfter
        );

        return new TransferResponse(fromUserId, toUserId, value, fromAfter, toAfter);
    }

    private Account lockAccount(Long userId) {
        return accountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("Account not found for user: " + userId));
    }

    private static void validateTransfer(Long fromUserId, Long toUserId, BigDecimal value) {
        if (fromUserId == null || toUserId == null) {
            throw new UnprocessableException("fromUserId and toUserId are required");
        }
        if (fromUserId.equals(toUserId)) {
            throw new UnprocessableException("Cannot transfer to yourself");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new UnprocessableException("Transfer value must be positive");
        }
        if (value.scale() > 2) {
            throw new UnprocessableException("Transfer value scale must be at most 2");
        }
    }
}
