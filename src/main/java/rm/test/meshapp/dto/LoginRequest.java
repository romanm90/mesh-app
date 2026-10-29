package rm.test.meshapp.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        String email,
        String phone,
        @NotBlank(message = "password is required") String password
) {
    @AssertTrue(message = "either email or phone must be provided")
    public boolean isLoginIdentityPresent() {
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        return hasEmail || hasPhone;
    }
}
