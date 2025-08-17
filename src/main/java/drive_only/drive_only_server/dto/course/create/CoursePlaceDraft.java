package drive_only.drive_only_server.dto.course.create;

import java.util.List;

public record CoursePlaceDraft(
        String placeId,
        String content,
        List<String> photoKeys,
        int sequence
) {
}
