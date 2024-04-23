package wander.wise.application.dto.social.link;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record CreateSocialLinkRequestDto(
        @NotNull Long userId,
        @NotBlank String name,
        @NotBlank @URL String link) {
}
