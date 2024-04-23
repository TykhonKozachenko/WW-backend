package wander.wise.application.dto.user.update;

import jakarta.validation.constraints.Email;

public record RestorePasswordRequestDto(@Email String email) {
}
