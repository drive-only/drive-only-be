package drive_only.drive_only_server.dto.place.nearbySearch;

import drive_only.drive_only_server.dto.place.search.PlaceSearchResponse;
import java.util.List;

public record NearbyPlacesResponse(
        Long coursePlaceId,
        Long placeId,
        String name,
        List<PlaceSearchResponse> nearbyPlaces
) {
}
