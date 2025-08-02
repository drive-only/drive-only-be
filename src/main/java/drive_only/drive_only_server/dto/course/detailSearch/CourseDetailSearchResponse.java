package drive_only.drive_only_server.dto.course.detailSearch;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.domain.Member;
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
        Boolean isMine,
        Boolean isLiked
) {
    public static CourseDetailSearchResponse from(Course course, List<CoursePlace> coursePlaces, Member loginMember) {
        boolean isMine = false;
        if (loginMember != null) {
            isMine = course.isWrittenBy(loginMember);
        }
        return new CourseDetailSearchResponse(
                course.getId(),
                course.getTitle(),
                course.getMember().getProfileImageUrl(),
                course.getMember().getNickname(),
                course.getCreatedDate(),
                CategoryResponse.from(course),
                createTagResponse(course),
                createCoursePlaceSearchResponse(coursePlaces),
                course.getRecommendation(),
                course.getDifficulty(),
                course.getLikeCount(),
                course.getViewCount(),
                isMine,
                course.isLiked()
        );
    }

    private static List<TagResponse> createTagResponse(Course course) {
        return course.getTags().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName()))
                .toList();
    }

    private static List<CoursePlaceSearchResponse> createCoursePlaceSearchResponse(List<CoursePlace> coursePlaces) {
        return coursePlaces.stream()
                .map(CoursePlaceSearchResponse::from)
                .toList();
    }
}
