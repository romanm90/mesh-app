package rm.test.meshapp.dto;

import java.math.BigDecimal;

public record TransferResponse(
        Long fromUserId,
        Long toUserId,
        BigDecimal value,
        BigDecimal fromBalance,
        BigDecimal toBalance
) {
}
