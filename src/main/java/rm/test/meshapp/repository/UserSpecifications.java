package rm.test.meshapp.repository;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import rm.test.meshapp.entity.EmailData;
import rm.test.meshapp.entity.PhoneData;
import rm.test.meshapp.entity.User;

import java.time.LocalDate;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> dateOfBirthAfter(LocalDate dateOfBirth) {
        return (root, query, cb) -> {
            if (dateOfBirth == null) {
                return null;
            }
            return cb.greaterThan(root.get("dateOfBirth"), dateOfBirth);
        };
    }

    public static Specification<User> nameStartsWith(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(root.get("name"), name + "%");
        };
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) {
                return null;
            }
            if (query != null) {
                query.distinct(true);
            }
            Join<User, EmailData> emails = root.join("emails");
            return cb.equal(emails.get("email"), email);
        };
    }

    public static Specification<User> hasPhone(String phone) {
        return (root, query, cb) -> {
            if (phone == null || phone.isBlank()) {
                return null;
            }
            if (query != null) {
                query.distinct(true);
            }
            Join<User, PhoneData> phones = root.join("phones");
            return cb.equal(phones.get("phone"), phone);
        };
    }

    public static Specification<User> withFilters(LocalDate dateOfBirth, String phone, String name, String email) {
        return Specification.allOf(
                dateOfBirthAfter(dateOfBirth),
                hasPhone(phone),
                nameStartsWith(name),
                hasEmail(email)
        );
    }
}
