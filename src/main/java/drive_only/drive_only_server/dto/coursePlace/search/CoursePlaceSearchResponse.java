package drive_only.drive_only_server.dto.coursePlace.search;

import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.dto.photo.PhotoResponse;
import java.util.List;

public record CoursePlaceSearchResponse(
        Long coursePlaceId,
        Long placeId,
        String placeType,
        String name,
        String address,
        String content,
        List<PhotoResponse> photoUrls,
        double lat,
        double lng,
        int sequence
) {
    public static CoursePlaceSearchResponse from(CoursePlace coursePlace) {
        return new CoursePlaceSearchResponse(
                coursePlace.getId(),
                coursePlace.getPlace().getId(),
                coursePlace.getPlaceType(),
                coursePlace.getName(),
                coursePlace.getPlace().getAddress(),
                coursePlace.getContent(),
                createPhotoResponse(coursePlace),
                coursePlace.getPlace().getLat(),
                coursePlace.getPlace().getLng(),
                coursePlace.getSequence()
        );
    }

    private static List<PhotoResponse> createPhotoResponse(CoursePlace coursePlace) {
        return coursePlace.getPhotos().stream()
                .map(photo -> new PhotoResponse(photo.getId(), photo.getUrl()))
                .toList();
    }
}
