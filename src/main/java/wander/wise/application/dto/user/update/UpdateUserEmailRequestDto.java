package wander.wise.application.dto.user.update;

import jakarta.validation.constraints.Email;

public record UpdateUserEmailRequestDto(@Email String email) {
}
