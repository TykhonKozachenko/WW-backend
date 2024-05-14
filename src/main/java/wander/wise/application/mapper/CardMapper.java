package wander.wise.application.mapper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import wander.wise.application.config.MapperConfig;
import wander.wise.application.dto.ai.AiResponseDto;
import wander.wise.application.dto.card.CardDto;
import wander.wise.application.dto.card.CreateCardRequestDto;
import wander.wise.application.model.Card;

@Mapper(config = MapperConfig.class, uses = {CommentMapper.class})
public interface CardMapper {
    Card aiResponseToCard(AiResponseDto aiResponseDto);

    @Mapping(target = "name", source = "fullName",
            qualifiedByName = "fullNameToName")
    @Mapping(target = "whereIs", source = "fullName",
            qualifiedByName = "fullNameToWhereIs")
    @Mapping(target = "tripTypes", source = "tripTypes",
            qualifiedByName = "stringToSet")
    @Mapping(target = "specialRequirements", source = "specialRequirements",
            qualifiedByName = "stringToSet")
    @Mapping(target = "whyThisPlace", source = "whyThisPlace",
            qualifiedByName = "stringToSet")
    @Mapping(target = "imageLinks", source = "imageLinks",
            qualifiedByName = "stringToSet")
    @Mapping(target = "comments", source = "card.comments",
            qualifiedByName = "toCommentDtoSet")
    CardDto toDto(Card card);

    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "tripTypes", ignore = true)
    @Mapping(target = "specialRequirements", ignore = true)
    @Mapping(target = "whyThisPlace", ignore = true)
    @Mapping(target = "imageLinks", ignore = true)
    Card toModel(CreateCardRequestDto requestDto);

    @AfterMapping
    default void afterMappingToModel(@MappingTarget Card card, CreateCardRequestDto requestDto) {
        card.setFullName(new StringBuilder()
                .append(requestDto.name()).append("|")
                .append(requestDto.populatedLocality()).append("|")
                .append(requestDto.region()).append("|")
                .append(requestDto.country()).append("|")
                .append(requestDto.continent()).toString());
        card.setTripTypes(String.join("|", requestDto.tripTypes()));
        card.setSpecialRequirements(String.join("|", requestDto.specialRequirements()));
        card.setWhyThisPlace(String.join("|", requestDto.whyThisPlace()));
    }

    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "tripTypes", ignore = true)
    @Mapping(target = "specialRequirements", ignore = true)
    @Mapping(target = "whyThisPlace", ignore = true)
    @Mapping(target = "imageLinks", ignore = true)
    Card updateCardFromRequestDto(@MappingTarget Card card, CreateCardRequestDto requestDto);

    @AfterMapping
    default void afterMappingUpdateCardFromRequestDto(@MappingTarget Card card,
                                                      CreateCardRequestDto requestDto) {
        afterMappingToModel(card, requestDto);
    }

    @Named("stringToSet")
    default Set<String> stringToSet(String field) {
        return Arrays.stream(field.split("\\|"))
                .collect(Collectors.toSet());
    }

    @Named("fullNameToName")
    default String fullNameToName(String fullName) {
        return fullName.split("\\|")[0];
    }

    @Named("fullNameToWhereIs")
    default String fullNameToWhereIs(String fullName) {
        String[] fullNameArray = fullName.split("\\|");
        return Arrays.toString(Arrays.copyOfRange(
                fullNameArray,
                1,
                fullNameArray.length))
                .replace("[", "")
                .replace("]", "");
    }

    @Named("cardsToCardDtos")
    default Set<CardDto> cardsTocardDtos(Set<Card> cards) {
        return cards.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }
}
