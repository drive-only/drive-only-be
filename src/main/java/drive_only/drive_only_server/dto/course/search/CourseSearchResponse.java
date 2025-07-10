package drive_only.drive_only_server.dto.course.search;

import drive_only.drive_only_server.dto.category.CategoryResponse;
import java.util.List;

public record CourseSearchResponse(
        Long courseId,
        String nickname,
        String createdDate,
        String title,
        String thumbnailUrl,
        List<String> coursePlaceNames,
        CategoryResponse categoryResponse,
        int likeCount,
        int viewCount
) {
}
