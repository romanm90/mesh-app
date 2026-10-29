package rm.test.meshapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull
        Long toUserId,

        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        @Digits(integer = 17, fraction = 2)
        BigDecimal value
) {
}
