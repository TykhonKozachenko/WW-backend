package wander.wise.application.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import wander.wise.application.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByPseudonym(String pseudonym);

    Optional<User> findByEmail(String email);
}
