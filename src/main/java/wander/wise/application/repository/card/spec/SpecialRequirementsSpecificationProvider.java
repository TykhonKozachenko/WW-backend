package wander.wise.application.repository.card.spec;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import wander.wise.application.model.Card;
import wander.wise.application.repository.SpecificationProvider;

@Component
public class SpecialRequirementsSpecificationProvider implements SpecificationProvider<Card> {
    private static final String KEY = "specialRequirements";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public Specification<Card> getSpecification(String[] params) {
        return (((root, query, criteriaBuilder) -> {
            // Create a predicate for each param
            List<Predicate> predicates = new ArrayList<>();
            for (String param : params) {
                Predicate predicate = criteriaBuilder.like(root.get(KEY), "%" + param + "%");
                predicates.add(predicate);
            }
            // Combine all predicates using 'or' condition
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }));
    }
}