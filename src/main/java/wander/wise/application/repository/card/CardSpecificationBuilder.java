package wander.wise.application.repository.card;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import wander.wise.application.dto.card.CardSearchParameters;
import wander.wise.application.model.Card;
import wander.wise.application.repository.SpecificationBuilder;
import wander.wise.application.repository.SpecificationProviderManager;

@Component
@RequiredArgsConstructor
public class CardSpecificationBuilder implements SpecificationBuilder<Card, CardSearchParameters> {
    private final SpecificationProviderManager<Card> cardSpecificationProviderManager;

    @Override
    public Specification<Card> build(CardSearchParameters searchParameters) {
        Specification<Card> spec = Specification.where(null);
        if (searchParameters.tripTypes() != null
                && searchParameters.tripTypes().length > 0) {
            spec = spec.and(cardSpecificationProviderManager
                    .getSpecificationProvider("tripTypes")
                    .getSpecification(searchParameters.tripTypes()));
        }
        if (searchParameters.climate() != null
                && searchParameters.climate().length > 0) {
            spec = spec.and(cardSpecificationProviderManager
                    .getSpecificationProvider("climate")
                    .getSpecification(searchParameters.climate()));
        }
        if (searchParameters.specialRequirements() != null
                && searchParameters.specialRequirements().length > 0) {
            spec = spec.and(cardSpecificationProviderManager
                    .getSpecificationProvider("specialRequirements")
                    .getSpecification(searchParameters.specialRequirements()));
        }
        if (searchParameters.travelDistance() != null
                && searchParameters.travelDistance().length > 0) {
            spec = spec.and(cardSpecificationProviderManager
                    .getSpecificationProvider("fullName")
                    .getSpecification(searchParameters.travelDistance()));
        }
        if (searchParameters.author() != null
                && searchParameters.author().length > 0) {
            spec = spec.and(cardSpecificationProviderManager
                    .getSpecificationProvider("author")
                    .getSpecification(searchParameters.author()));
        }
        return spec;
    }
}
