package rm.test.meshapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rm.test.meshapp.config.CacheConfig;
import rm.test.meshapp.dto.EmailRequest;
import rm.test.meshapp.dto.EmailResponse;
import rm.test.meshapp.entity.EmailData;
import rm.test.meshapp.entity.User;
import rm.test.meshapp.exception.ConflictException;
import rm.test.meshapp.exception.ForbiddenException;
import rm.test.meshapp.exception.NotFoundException;
import rm.test.meshapp.exception.UnprocessableException;
import rm.test.meshapp.repository.EmailDataRepository;
import rm.test.meshapp.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDataService {

    private final EmailDataRepository emailDataRepository;
    private final UserRepository userRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.USER_CACHE, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.USERS_SEARCH_CACHE, allEntries = true)
    })
    public EmailResponse add(Long userId, EmailRequest request) {
        String email = normalize(request.email());
        ensureEmailAvailable(email, null);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        EmailData emailData = new EmailData();
        emailData.setUser(user);
        emailData.setEmail(email);
        EmailData saved = emailDataRepository.save(emailData);

        log.info("Added email id={} for userId={}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.USER_CACHE, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.USERS_SEARCH_CACHE, allEntries = true)
    })
    public EmailResponse update(Long userId, Long emailId, EmailRequest request) {
        EmailData emailData = requireOwnedEmail(userId, emailId);
        String email = normalize(request.email());
        ensureEmailAvailable(email, emailId);

        emailData.setEmail(email);
        EmailData saved = emailDataRepository.save(emailData);

        log.info("Updated email id={} for userId={}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.USER_CACHE, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.USERS_SEARCH_CACHE, allEntries = true)
    })
    public void delete(Long userId, Long emailId) {
        requireOwnedEmail(userId, emailId);

        if (emailDataRepository.countByUser_Id(userId) <= 1) {
            throw new UnprocessableException("Cannot delete the last email");
        }

        emailDataRepository.deleteById(emailId);
        log.info("Deleted email id={} for userId={}", emailId, userId);
    }

    private EmailData requireOwnedEmail(Long userId, Long emailId) {
        EmailData emailData = emailDataRepository.findById(emailId)
                .orElseThrow(() -> new NotFoundException("Email not found: " + emailId));

        if (!emailData.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Email does not belong to the current user");
        }
        return emailData;
    }

    private void ensureEmailAvailable(String email, Long excludeId) {
        boolean taken = excludeId == null
                ? emailDataRepository.existsByEmail(email)
                : emailDataRepository.existsByEmailAndIdNot(email, excludeId);
        if (taken) {
            throw new ConflictException("Email already in use: " + email);
        }
    }

    private static String normalize(String email) {
        return email.strip();
    }

    private static EmailResponse toResponse(EmailData emailData) {
        return new EmailResponse(emailData.getId(), emailData.getEmail());
    }
}
