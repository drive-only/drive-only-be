package drive_only.drive_only_server.dto.coursePlace.create;

import drive_only.drive_only_server.dto.photo.PhotoRequest;
import java.util.List;

public record CoursePlaceCreateRequest(
        String placeId,
        String content,
        List<PhotoRequest> photoUrls,
        int sequence
) {
}
