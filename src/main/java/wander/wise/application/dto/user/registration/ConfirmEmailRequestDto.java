package wander.wise.application.dto.user.registration;

import jakarta.validation.constraints.Email;

public record ConfirmEmailRequestDto(@Email String email) {
}
