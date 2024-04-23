package wander.wise.application.dto.user.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @Email String email,
        @Size(min = 8) String password) {
}
