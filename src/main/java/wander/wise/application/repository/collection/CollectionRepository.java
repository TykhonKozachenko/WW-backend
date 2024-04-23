package wander.wise.application.repository.collection;

import org.springframework.data.jpa.repository.JpaRepository;
import wander.wise.application.model.Collection;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findAllByUserEmail(String email);
}
