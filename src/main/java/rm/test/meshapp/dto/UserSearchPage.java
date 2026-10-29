package rm.test.meshapp.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Serializable cache payload for user search (avoids fragile PageImpl Redis serialization).
 */
public record UserSearchPage(
        List<UserResponse> content,
        long totalElements,
        int pageNumber,
        int pageSize
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static UserSearchPage from(Page<UserResponse> page) {
        return new UserSearchPage(
                List.copyOf(page.getContent()),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }

    public Page<UserResponse> toPage() {
        return new PageImpl<>(content, PageRequest.of(pageNumber, pageSize), totalElements);
    }
}
