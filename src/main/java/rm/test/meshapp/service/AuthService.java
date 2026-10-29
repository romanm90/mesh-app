package rm.test.meshapp.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rm.test.meshapp.dto.LoginRequest;
import rm.test.meshapp.dto.LoginResponse;
import rm.test.meshapp.entity.EmailData;
import rm.test.meshapp.entity.PhoneData;
import rm.test.meshapp.entity.User;
import rm.test.meshapp.exception.UnauthorizedException;
import rm.test.meshapp.repository.EmailDataRepository;
import rm.test.meshapp.repository.PhoneDataRepository;
import rm.test.meshapp.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String loginKey = loginKey(request);
        User user = resolveUser(request);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password for {}", loginKey);
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId());
        log.info("Login success: userId={} via {}", user.getId(), loginKey);
        return new LoginResponse(token);
    }

    private User resolveUser(LoginRequest request) {
        if (request.email() != null && !request.email().isBlank()) {
            return emailDataRepository.findByEmailWithUser(request.email().trim())
                    .map(EmailData::getUser)
                    .orElseGet(() -> {
                        log.warn("Login failed: unknown email");
                        throw new UnauthorizedException("Invalid credentials");
                    });
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            return phoneDataRepository.findByPhoneWithUser(request.phone().trim())
                    .map(PhoneData::getUser)
                    .orElseGet(() -> {
                        log.warn("Login failed: unknown phone");
                        throw new UnauthorizedException("Invalid credentials");
                    });
        }
        log.warn("Login failed: email/phone missing");
        throw new UnauthorizedException("Invalid credentials");
    }

    private static String loginKey(LoginRequest request) {
        if (request.email() != null && !request.email().isBlank()) {
            return "email";
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            return "phone";
        }
        return "unknown";
    }
}
