package rm.test.meshapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PhoneRequest(
        @NotBlank
        @Pattern(regexp = "^7\\d{10}$", message = "must match ^7\\d{10}$")
        @Size(max = 13)
        String phone
) {
}
