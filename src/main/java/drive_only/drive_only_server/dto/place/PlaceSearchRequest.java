package drive_only.drive_only_server.dto.place;

public record PlaceSearchRequest(
        String type,
        String keyword,
        String region,
        String subRegion
) {
}
