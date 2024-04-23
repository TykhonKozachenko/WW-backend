package wander.wise.application.dto.comment;

public record CommentDto(
        Long id,
        String author,
        String timeStamp,
        String text,
        Integer stars) {
}
