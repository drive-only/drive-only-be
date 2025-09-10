package drive_only.drive_only_server.dto.place.search;

public record PlaceSearchRequest(
        String type,
        String keyword,
        String region,
        String subRegion
) {
}
