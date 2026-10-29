package rm.test.meshapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import rm.test.meshapp.dto.TransferResponse;
import rm.test.meshapp.entity.Account;
import rm.test.meshapp.entity.User;
import rm.test.meshapp.exception.NotFoundException;
import rm.test.meshapp.exception.UnprocessableException;
import rm.test.meshapp.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountRepository);
    }

    @Test
    void transfer_happyPath_updatesBalances() {
        Account from = account(1L, "100.00");
        Account to = account(2L, "50.00");
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(to));

        TransferResponse response = transferService.transfer(1L, 2L, new BigDecimal("30.50"));

        assertThat(response.fromUserId()).isEqualTo(1L);
        assertThat(response.toUserId()).isEqualTo(2L);
        assertThat(response.value()).isEqualByComparingTo("30.50");
        assertThat(response.fromBalance()).isEqualByComparingTo("69.50");
        assertThat(response.toBalance()).isEqualByComparingTo("80.50");
        assertThat(from.getBalance()).isEqualByComparingTo("69.50");
        assertThat(to.getBalance()).isEqualByComparingTo("80.50");
    }

    @Test
    void transfer_locksAccountsInAscendingUserIdOrder() {
        Account lower = account(1L, "100.00");
        Account higher = account(5L, "100.00");
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(lower));
        when(accountRepository.findByUserIdForUpdate(5L)).thenReturn(Optional.of(higher));

        // from higher id to lower id — still lock 1 then 5
        transferService.transfer(5L, 1L, new BigDecimal("10.00"));

        InOrder order = inOrder(accountRepository);
        order.verify(accountRepository).findByUserIdForUpdate(1L);
        order.verify(accountRepository).findByUserIdForUpdate(5L);
        verifyNoMoreInteractions(accountRepository);

        assertThat(higher.getBalance()).isEqualByComparingTo("90.00");
        assertThat(lower.getBalance()).isEqualByComparingTo("110.00");
    }

    @Test
    void transfer_insufficientFunds_throws() {
        Account from = account(1L, "10.00");
        Account to = account(2L, "50.00");
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("10.01")))
                .isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("Insufficient funds");

        assertThat(from.getBalance()).isEqualByComparingTo("10.00");
        assertThat(to.getBalance()).isEqualByComparingTo("50.00");
    }

    @Test
    void transfer_selfTransfer_throws() {
        assertThatThrownBy(() -> transferService.transfer(1L, 1L, new BigDecimal("5.00")))
                .isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("yourself");

        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void transfer_nonPositiveValue_throws() {
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.ZERO))
                .isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("positive");

        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("-1.00")))
                .isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void transfer_valueScaleTooLarge_throws() {
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("1.001")))
                .isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("scale");
    }

    @Test
    void transfer_missingAccount_throws() {
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("1.00")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void concurrentTransfers_doNotOverdraw() throws Exception {
        Map<Long, Account> accounts = new ConcurrentHashMap<>();
        accounts.put(1L, account(1L, "100.00"));
        accounts.put(2L, account(2L, "0.00"));

        Map<Long, ReentrantLock> locks = new ConcurrentHashMap<>();
        locks.put(1L, new ReentrantLock());
        locks.put(2L, new ReentrantLock());

        ThreadLocal<Deque<ReentrantLock>> held = ThreadLocal.withInitial(ArrayDeque::new);

        AccountRepository lockingRepo = mock(AccountRepository.class);
        Answer<Optional<Account>> lockAndLoad = invocation -> {
            Long userId = invocation.getArgument(0);
            ReentrantLock lock = locks.get(userId);
            if (lock == null) {
                return Optional.empty();
            }
            lock.lock();
            held.get().push(lock);
            return Optional.ofNullable(accounts.get(userId));
        };
        when(lockingRepo.findByUserIdForUpdate(anyLong())).thenAnswer(lockAndLoad);

        TransferService service = new TransferService(lockingRepo);

        int threads = 20;
        BigDecimal amount = new BigDecimal("10.00");
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger insufficient = new AtomicInteger();

        try {
            Future<?>[] futures = new Future<?>[threads];
            for (int i = 0; i < threads; i++) {
                futures[i] = executor.submit(() -> {
                    await(start);
                    try {
                        service.transfer(1L, 2L, amount);
                        successes.incrementAndGet();
                    } catch (UnprocessableException ex) {
                        if (ex.getMessage().contains("Insufficient funds")) {
                            insufficient.incrementAndGet();
                        } else {
                            throw ex;
                        }
                    } finally {
                        Deque<ReentrantLock> stack = held.get();
                        while (!stack.isEmpty()) {
                            stack.pop().unlock();
                        }
                        held.remove();
                    }
                });
            }

            start.countDown();
            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(successes.get()).isEqualTo(10);
        assertThat(insufficient.get()).isEqualTo(10);
        assertThat(accounts.get(1L).getBalance()).isEqualByComparingTo("0.00");
        assertThat(accounts.get(2L).getBalance()).isEqualByComparingTo("100.00");
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting to start");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static Account account(Long userId, String balance) {
        User user = new User();
        user.setId(userId);

        Account account = new Account();
        account.setId(userId);
        account.setUser(user);
        account.setBalance(new BigDecimal(balance));
        account.setInitialBalance(new BigDecimal(balance));
        return account;
    }
}
