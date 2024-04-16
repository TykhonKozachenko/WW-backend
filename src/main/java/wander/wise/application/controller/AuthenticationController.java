package wander.wise.application.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wander.wise.application.dto.user.CreateUserRequestDto;
import wander.wise.application.dto.user.UserDto;
import wander.wise.application.service.user.UserService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;

    @PostMapping("/register")
    UserDto registerNewUser(@RequestBody CreateUserRequestDto requestDto) {
        return userService.save(requestDto);
    }
}
