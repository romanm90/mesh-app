package rm.test.meshapp.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record UserResponse(
        Long id,
        String name,
        LocalDate dateOfBirth,
        List<String> emails,
        List<String> phones
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
