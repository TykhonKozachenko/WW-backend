package wander.wise.application.dto.card;

import jakarta.validation.constraints.NotBlank;
import wander.wise.application.validation.map.link.MapLink;

public record CreateCardRequestDto(
        @NotBlank String fullName,
        @NotBlank String tripTypes,
        @NotBlank String climate,
        @NotBlank String specialRequirements,
        @NotBlank String description,
        @NotBlank String whyThisPlace,
        @NotBlank String imageLinks,
        @NotBlank @MapLink String mapLink) {
}
