package wander.wise.application.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import wander.wise.application.config.MapperConfig;
import wander.wise.application.dto.CommentDto;
import wander.wise.application.model.Comment;

@Mapper(config = MapperConfig.class)
public interface CommentMapper {
    CommentDto commentToDto(Comment comment);

    @Named("toCommentDtoSet")
    default Set<CommentDto> toCommentDtoList(Set<Comment> comments) {
        return comments.stream().map(this::commentToDto).collect(Collectors.toSet());
    }
}
