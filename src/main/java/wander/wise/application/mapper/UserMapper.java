package wander.wise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import wander.wise.application.config.MapperConfig;
import wander.wise.application.dto.user.CreateUserRequestDto;
import wander.wise.application.dto.user.UserDto;
import wander.wise.application.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toModel(CreateUserRequestDto requestDto);
}
