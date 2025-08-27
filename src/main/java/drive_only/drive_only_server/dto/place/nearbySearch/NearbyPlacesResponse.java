package drive_only.drive_only_server.dto.place.nearbySearch;

import drive_only.drive_only_server.domain.CoursePlace;
import java.util.List;

public record NearbyPlacesResponse(
        Long coursePlaceId,
        Long placeId,
        String placeName,
        List<NearbyPlaceSearchResponse> nearbyPlaces
) {
    public static NearbyPlacesResponse from(CoursePlace coursePlace, List<NearbyPlaceSearchResponse> searchResponses) {
        return new NearbyPlacesResponse(
                coursePlace.getId(),
                coursePlace.getPlace().getId(),
                coursePlace.getPlace().getName(),
                searchResponses
        );
    }
}
