package drive_only.drive_only_server.dto.course.search;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.dto.category.CategoryResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public record CourseSearchResponse(
        Long courseId,
        String profileImageUrl,
        String nickname,
        String createdDate,
        String title,
        String thumbnailUrl,
        List<String> coursePlaceNames,
        CategoryResponse category,
        int likeCount,
        int viewCount
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static CourseSearchResponse from(Course course) {
        return new CourseSearchResponse(
                course.getId(),
                course.getMember().getProfileImageUrl(),
                course.getMember().getNickname(),
                formatDate(course.getCreatedDate()),
                course.getTitle(),
                getCourseThumbnailUrl(course),
                getCoursePlaceNames(course),
                CategoryResponse.from(course),
                course.getLikeCount(),
                course.getViewCount()
        );
    }

    private static String formatDate(LocalDate createdDate) {
        return Optional.ofNullable(createdDate)
                .map(DATE_FORMATTER::format)
                .orElse("날짜 없음");
    }

    private static String getCourseThumbnailUrl(Course course) {
        return course.getCoursePlaces().get(0).getPhotos().get(0).getUrl();
    }

    private static List<String> getCoursePlaceNames(Course course) {
        return course.getCoursePlaces().stream()
                .map(CoursePlace::getPlaceName)
                .toList();
    }
}
