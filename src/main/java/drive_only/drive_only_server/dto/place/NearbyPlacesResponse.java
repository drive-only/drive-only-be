package drive_only.drive_only_server.dto.place;

import java.util.List;

public record NearbyPlacesResponse(
        Long coursePlaceId,
        Long placeId,
        String name,
        List<PlaceSearchResponse> nearbyPlaces
) {
}
