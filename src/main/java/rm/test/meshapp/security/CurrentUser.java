package rm.test.meshapp.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import rm.test.meshapp.exception.UnauthorizedException;

/**
 * Resolves the authenticated user id from the security context.
 * JwtAuthFilter (jwt-security) is expected to set the principal to {@link Long} USER_ID.
 */
@Component
public class CurrentUser {

    public Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof Number number) {
            return number.longValue();
        }
        if (principal instanceof String value && !"anonymousUser".equals(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }

        throw new UnauthorizedException("Authentication required");
    }
}
