package wander.wise.application.dto.card;

import wander.wise.application.dto.comment.CommentDto;

import java.util.Set;

public record CardWithoutDistanceDto(
        Long id,
        String name,
        String author,
        Set<String> tripTypes,
        String climate,
        Set<String> specialRequirements,
        String whereIs,
        String description,
        Set<String> whyThisPlace,
        Set<String> imageLinks,
        String mapLink,
        Long likes,
        Set<CommentDto> comments,
        boolean shown) {
}
