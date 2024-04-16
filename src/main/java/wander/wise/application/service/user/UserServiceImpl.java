package wander.wise.application.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wander.wise.application.dto.user.CreateUserRequestDto;
import wander.wise.application.dto.user.UserDto;
import wander.wise.application.mapper.UserMapper;
import wander.wise.application.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto save(CreateUserRequestDto requestDto) {
        return userMapper.toDto(userRepository.save(userMapper.toModel(requestDto)));
    }
}
