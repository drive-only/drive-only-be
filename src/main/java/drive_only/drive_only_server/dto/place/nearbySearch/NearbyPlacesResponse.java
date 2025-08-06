package drive_only.drive_only_server.dto.place.nearbySearch;

import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.dto.place.search.PlaceSearchResponse;
import java.util.List;

public record NearbyPlacesResponse(
        Long coursePlaceId,
        Long placeId,
        String placeName,
        List<PlaceSearchResponse> nearbyPlaces
) {
    public static NearbyPlacesResponse from(CoursePlace coursePlace, List<PlaceSearchResponse> searchResponses) {
        return new NearbyPlacesResponse(
                coursePlace.getId(),
                coursePlace.getPlace().getId(),
                coursePlace.getPlace().getName(),
                searchResponses
        );
    }
}
