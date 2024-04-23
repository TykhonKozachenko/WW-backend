package wander.wise.application.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import wander.wise.application.dto.user.login.LoginRequestDto;
import wander.wise.application.dto.user.login.LoginResponseDto;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDto authenticate(LoginRequestDto requestDto) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.email(),
                        requestDto.password()));
        String token = jwtUtil.generateToken(authentication.getName());
        return new LoginResponseDto(token);
    }

    public LoginResponseDto refreshJwt(String email) {
        String token = jwtUtil.generateToken(email);
        return new LoginResponseDto(token);
    }
}
