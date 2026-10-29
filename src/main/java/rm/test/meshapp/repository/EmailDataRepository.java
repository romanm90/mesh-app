package rm.test.meshapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rm.test.meshapp.entity.EmailData;

import java.util.List;
import java.util.Optional;

public interface EmailDataRepository extends JpaRepository<EmailData, Long> {

    Optional<EmailData> findByEmail(String email);

    @Query("select e from EmailData e join fetch e.user where e.email = :email")
    Optional<EmailData> findByEmailWithUser(@Param("email") String email);

    boolean existsByEmail(String email);

    long countByEmail(String email);

    List<EmailData> findByUser_Id(Long userId);

    long countByUser_Id(Long userId);

    boolean existsByEmailAndIdNot(String email, Long id);
}
