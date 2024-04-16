package wander.wise.application.service.user;

import wander.wise.application.dto.user.CreateUserRequestDto;
import wander.wise.application.dto.user.UserDto;

public interface UserService {
    UserDto save(CreateUserRequestDto requestDto);
}
