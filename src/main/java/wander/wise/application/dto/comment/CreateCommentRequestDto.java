package wander.wise.application.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequestDto(
        @NotNull Long cardId,
        @NotBlank String text,
        @NotNull Integer stars) {
}
