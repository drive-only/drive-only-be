package drive_only.drive_only_server.dto.place.nearbySearch;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.Place;

public record NearbyPlaceSearchResponse(
        Long placeId,
        String placeType,
        String placeName,
        String placeAddress,
        String thumbnailUrl,
        String usetime,
        String restdate,
        String phoneNum,
        Double lat,
        Double lng,
        boolean isSavedPlace
) {
    public static NearbyPlaceSearchResponse from(Place place, Member loginMember) {
        return new NearbyPlaceSearchResponse(
                place.getId(),
                getType(place.getContentTypeId()),
                place.getName(),
                place.getAddress(),
                place.getThumbNailUrl(),
                place.getUseTime(),
                place.getRestDate(),
                place.getPhoneNum(),
                place.getLat(),
                place.getLng(),
                place.isSavedFrom(loginMember)
        );
    }

    private static String getType(int contentTypeId) {
        if (contentTypeId == 12 || contentTypeId == 14 || contentTypeId ==38) {
            return "tourist-spot";
        }
        if (contentTypeId == 39) {
            return "restaurant";
        }
        return "";
    }
}
