package wander.wise.application.mapper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import wander.wise.application.config.MapperConfig;
import wander.wise.application.dto.ai.AiResponseDto;
import wander.wise.application.dto.card.CardDto;
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
}