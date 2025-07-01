package drive_only.drive_only_server.dto.coursePlace.search;

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
) {}
