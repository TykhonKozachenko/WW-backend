package wander.wise.application.service.card;

import wander.wise.application.dto.card.CardDto;
import wander.wise.application.dto.card.CreateCardRequestDto;

import java.util.List;

public interface CardService {
    CardDto saveCard(CreateCardRequestDto requestDto);

    List<CardDto> findAll();
}
