package wander.wise.application.dto.card;

public record CardSearchParameters(
        String startLocation,
        String[] tripTypes,
        String[] climate,
        String[] specialRequirements,
        String[] travelDistance,
        String[] author) {
}
