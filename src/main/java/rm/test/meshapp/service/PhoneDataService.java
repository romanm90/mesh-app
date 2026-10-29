package rm.test.meshapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rm.test.meshapp.config.CacheConfig;
import rm.test.meshapp.dto.PhoneRequest;
import rm.test.meshapp.dto.PhoneResponse;
import rm.test.meshapp.entity.PhoneData;
import rm.test.meshapp.entity.User;
import rm.test.meshapp.exception.ConflictException;
import rm.test.meshapp.exception.ForbiddenException;
import rm.test.meshapp.exception.NotFoundException;
import rm.test.meshapp.exception.UnprocessableException;
import rm.test.meshapp.repository.PhoneDataRepository;
import rm.test.meshapp.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneDataService {

    private final PhoneDataRepository phoneDataRepository;
    private final UserRepository userRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.USER_CACHE, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.USERS_SEARCH_CACHE, allEntries = true)
    })
    public PhoneResponse add(Long userId, PhoneRequest request) {
        String phone = normalize(request.phone());
        ensurePhoneAvailable(phone, null);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        PhoneData phoneData = new PhoneData();
        phoneData.setUser(user);
        phoneData.setPhone(phone);
        PhoneData saved = phoneDataRepository.save(phoneData);

        log.info("Added phone id={} for userId={}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.USER_CACHE, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.USERS_SEARCH_CACHE, allEntries = true)
    })
    public PhoneResponse update(Long userId, Long phoneId, PhoneRequest request) {
        PhoneData phoneData = requireOwnedPhone(userId, phoneId);
        String phone = normalize(request.phone());
        ensurePhoneAvailable(phone, phoneId);

        phoneData.setPhone(phone);
        PhoneData saved = phoneDataRepository.save(phoneData);

        log.info("Updated phone id={} for userId={}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.USER_CACHE, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.USERS_SEARCH_CACHE, allEntries = true)
    })
    public void delete(Long userId, Long phoneId) {
        requireOwnedPhone(userId, phoneId);

        if (phoneDataRepository.countByUser_Id(userId) <= 1) {
            throw new UnprocessableException("Cannot delete the last phone");
        }

        phoneDataRepository.deleteById(phoneId);
        log.info("Deleted phone id={} for userId={}", phoneId, userId);
    }

    private PhoneData requireOwnedPhone(Long userId, Long phoneId) {
        PhoneData phoneData = phoneDataRepository.findById(phoneId)
                .orElseThrow(() -> new NotFoundException("Phone not found: " + phoneId));

        if (!phoneData.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Phone does not belong to the current user");
        }
        return phoneData;
    }

    private void ensurePhoneAvailable(String phone, Long excludeId) {
        boolean taken = excludeId == null
                ? phoneDataRepository.existsByPhone(phone)
                : phoneDataRepository.existsByPhoneAndIdNot(phone, excludeId);
        if (taken) {
            throw new ConflictException("Phone already in use: " + phone);
        }
    }

    private static String normalize(String phone) {
        return phone.strip();
    }

    private static PhoneResponse toResponse(PhoneData phoneData) {
        return new PhoneResponse(phoneData.getId(), phoneData.getPhone());
    }
}
