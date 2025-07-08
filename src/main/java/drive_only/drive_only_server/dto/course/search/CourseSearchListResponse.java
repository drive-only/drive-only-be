package drive_only.drive_only_server.dto.course.search;

import drive_only.drive_only_server.dto.meta.Meta;
import java.util.List;

public record CourseSearchListResponse(
        List<CourseSearchResponse> data,
        Meta meta
) {
}
