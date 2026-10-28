package rm.test.meshapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rm.test.meshapp.entity.EmailData;

import java.util.List;
import java.util.Optional;

public interface EmailDataRepository extends JpaRepository<EmailData, Long> {

    Optional<EmailData> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByEmail(String email);

    List<EmailData> findByUser_Id(Long userId);

    long countByUser_Id(Long userId);

    boolean existsByEmailAndIdNot(String email, Long id);
}
