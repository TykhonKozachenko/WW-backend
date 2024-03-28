package wander.wise.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wander.wise.application.model.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
}
