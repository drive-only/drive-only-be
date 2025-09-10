package drive_only.drive_only_server.dto.coursePlace.search;

import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.photo.PhotoResponse;
import java.util.List;

public record CoursePlaceSearchResponse(
        Long coursePlaceId,
        Long placeId,
        String placeType,
        String placeName,
        String placeAddress,
        String content,
        List<PhotoResponse> photoUrls,
        double lat,
        double lng,
        int sequence,
        boolean isSavedPlace,
        Long savedPlaceId
) {
    public static CoursePlaceSearchResponse from(CoursePlace coursePlace, Member loginMember) {
        return new CoursePlaceSearchResponse(
                coursePlace.getId(),
                coursePlace.getPlace().getId(),
                coursePlace.getPlaceType(),
                coursePlace.getPlaceName(),
                coursePlace.getPlace().getAddress(),
                coursePlace.getContent(),
                createPhotoResponse(coursePlace),
                coursePlace.getPlace().getLat(),
                coursePlace.getPlace().getLng(),
                coursePlace.getSequence(),
                coursePlace.getPlace().isSavedFrom(loginMember),
                getSavedPlaceId(coursePlace, loginMember)
        );
    }

    private static List<PhotoResponse> createPhotoResponse(CoursePlace coursePlace) {
        return coursePlace.getPhotos().stream()
                .map(photo -> new PhotoResponse(photo.getId(), photo.getUrl()))
                .toList();
    }

    private static Long getSavedPlaceId(CoursePlace coursePlace, Member loginMember) {
        Long savedPlaceId = null;
        if (loginMember != null) {
            savedPlaceId = loginMember.getSavedPlaceId(coursePlace.getPlace().getId());
        }
        return savedPlaceId;
    }
}
