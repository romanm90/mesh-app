package rm.test.meshapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rm.test.meshapp.config.CacheConfig;
import rm.test.meshapp.dto.UserResponse;
import rm.test.meshapp.dto.UserSearchPage;
import rm.test.meshapp.entity.EmailData;
import rm.test.meshapp.entity.PhoneData;
import rm.test.meshapp.entity.User;
import rm.test.meshapp.exception.NotFoundException;
import rm.test.meshapp.repository.UserRepository;
import rm.test.meshapp.repository.UserSpecifications;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(
            cacheNames = CacheConfig.USERS_SEARCH_CACHE,
            key = "T(java.util.Objects).toString(#dateOfBirth) + '|' + #phone + '|' + #name + '|' + #email + '|' + #page + '|' + #size"
    )
    @Transactional(readOnly = true)
    public UserSearchPage searchPage(
            LocalDate dateOfBirth,
            String phone,
            String name,
            String email,
            int page,
            int size
    ) {
        log.debug(
                "Searching users dateOfBirth={} phone={} name={} email={} page={} size={}",
                dateOfBirth, phone, name, email, page, size
        );
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> result = userRepository
                .findAll(UserSpecifications.withFilters(dateOfBirth, phone, name, email), pageable)
                .map(this::toResponse);
        return UserSearchPage.from(result);
    }

    @Cacheable(cacheNames = CacheConfig.USER_CACHE, key = "#userId")
    @Transactional(readOnly = true)
    public UserResponse getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getDateOfBirth(),
                user.getEmails().stream().map(EmailData::getEmail).toList(),
                user.getPhones().stream().map(PhoneData::getPhone).toList()
        );
    }
}
