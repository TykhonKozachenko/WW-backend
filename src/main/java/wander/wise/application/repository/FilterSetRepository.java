package wander.wise.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wander.wise.application.model.FilterSet;

public interface FilterSetRepository extends JpaRepository<FilterSet, Long> {
}
