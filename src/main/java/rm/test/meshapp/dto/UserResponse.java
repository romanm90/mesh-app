package rm.test.meshapp.dto;

import java.time.LocalDate;
import java.util.List;

public record UserResponse(
        Long id,
        String name,
        LocalDate dateOfBirth,
        List<String> emails,
        List<String> phones
) {
}
