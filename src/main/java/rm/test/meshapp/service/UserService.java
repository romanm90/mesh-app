package rm.test.meshapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rm.test.meshapp.dto.UserResponse;
import rm.test.meshapp.entity.EmailData;
import rm.test.meshapp.entity.PhoneData;
import rm.test.meshapp.entity.User;
import rm.test.meshapp.repository.UserRepository;
import rm.test.meshapp.repository.UserSpecifications;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> search(LocalDate dateOfBirth, String phone, String name, String email, Pageable pageable) {
        return userRepository
                .findAll(UserSpecifications.withFilters(dateOfBirth, phone, name, email), pageable)
                .map(this::toResponse);
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
