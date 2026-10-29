package rm.test.meshapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rm.test.meshapp.entity.PhoneData;

import java.util.List;
import java.util.Optional;

public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {

    Optional<PhoneData> findByPhone(String phone);

    @Query("select p from PhoneData p join fetch p.user where p.phone = :phone")
    Optional<PhoneData> findByPhoneWithUser(@Param("phone") String phone);

    boolean existsByPhone(String phone);

    long countByPhone(String phone);

    List<PhoneData> findByUser_Id(Long userId);

    long countByUser_Id(Long userId);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
