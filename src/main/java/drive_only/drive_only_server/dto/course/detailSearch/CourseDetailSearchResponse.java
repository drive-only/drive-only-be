package drive_only.drive_only_server.dto.course.detailSearch;

import drive_only.drive_only_server.dto.category.CategoryResponse;
import drive_only.drive_only_server.dto.coursePlace.search.CoursePlaceSearchResponse;
import drive_only.drive_only_server.dto.tag.TagResponse;
import java.time.LocalDate;
import java.util.List;

public record CourseDetailSearchResponse(
        Long courseId,
        String title,
        String profileImageUrl,
        String nickname,
        LocalDate createdDate,
        CategoryResponse category,
        List<TagResponse> tags,
        List<CoursePlaceSearchResponse> coursePlaces,
        double recommendation,
        double difficulty,
        int likeCount,
        int viewCount,
        Boolean isLiked
) {}
