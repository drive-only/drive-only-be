package drive_only.drive_only_server.dto.course.search;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.dto.category.CategoryResponse;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public record MyCourseSearchResponse(
        Long courseId,
        String nickname,
        String createdDate,
        String title,
        String thumbnailUrl,
        List<String> coursePlaceNames,
        CategoryResponse category,
        int likeCount,
        int viewCount
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static MyCourseSearchResponse from(Course course) {
        return new MyCourseSearchResponse(
                course.getId(),
                course.getMember().getNickname(),
                Optional.ofNullable(course.getCreatedDate()).map(FORMATTER::format).orElse("날짜 없음"),
                course.getTitle(),
                course.getCoursePlaces().stream()
                        .flatMap(cp -> cp.getPhotos().stream())
                        .map(photo -> photo.getUrl())
                        .findFirst()
                        .orElse(null),
                course.getCoursePlaces().stream()
                        .sorted((a, b) -> Integer.compare(a.getSequence(), b.getSequence()))
                        .map(CoursePlace::getPlaceName)
                        .toList(),
                CategoryResponse.from(course),
                course.getLikeCount(),
                course.getViewCount()
        );
    }
}
