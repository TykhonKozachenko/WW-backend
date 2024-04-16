package wander.wise.application.dto.maps;

public record MapsResponseDto(
        String mapLink,
        double latitude,
        double longitude) {
}
