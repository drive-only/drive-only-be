package drive_only.drive_only_server.dto.coursePlace.create;

import drive_only.drive_only_server.dto.photo.PhotoRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CoursePlaceCreateRequest(
        String placeId,
        String placeType,
        String placeName,
        String placeAddress,

        @NotBlank
        String content,
        List<PhotoRequest> photoUrls,
        int sequence
) {}
