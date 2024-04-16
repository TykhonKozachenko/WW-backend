package wander.wise.application.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import wander.wise.application.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
