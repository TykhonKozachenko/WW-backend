package wander.wise.application.dto;

public record CommentDto(
        Long id,
        String author,
        String timeStamp,
        String text,
        Integer stars) {
}
