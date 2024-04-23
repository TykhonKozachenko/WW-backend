package wander.wise.application.dto.collection;

import wander.wise.application.dto.card.CardWithoutDistanceDto;

import java.util.Set;

public record CollectionDto(
        Long id,
        String author,
        String name,
        String imageLink,
        Set<CardWithoutDistanceDto> cardWithoutDistanceDtos,
        boolean isPublic) {
}
