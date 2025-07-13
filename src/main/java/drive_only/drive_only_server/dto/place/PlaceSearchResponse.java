package drive_only.drive_only_server.dto.place;

public record PlaceSearchResponse(
        Long placeId,
        String placeType,
        String name,
        String thumbnailUrl,
        String usetime,
        String restdate,
        String phoneNum,
        Double lat,
        Double lng
) {
}
