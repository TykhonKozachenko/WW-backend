package wander.wise.application.service.card;

import java.util.List;
import org.springframework.data.domain.Pageable;
import wander.wise.application.dto.card.CardDto;
import wander.wise.application.dto.card.CardSearchParameters;
import wander.wise.application.dto.card.CardWithoutDistanceDto;
import wander.wise.application.dto.card.CreateCardRequestDto;

public interface CardService {
    CardWithoutDistanceDto saveCard(CreateCardRequestDto requestDto);

    List<CardDto> search(Pageable pageable, CardSearchParameters searchParameters);

    List<CardWithoutDistanceDto> saveAll(List<CreateCardRequestDto> requestDtos);
}
