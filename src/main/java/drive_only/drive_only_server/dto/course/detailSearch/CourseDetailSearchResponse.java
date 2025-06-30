package drive_only.drive_only_server.dto.course.detailSearch;

import drive_only.drive_only_server.dto.category.CategoryResponse;
import drive_only.drive_only_server.dto.coursePlace.search.CoursePlaceSearchResponse;
import drive_only.drive_only_server.dto.tag.TagResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class CourseDetailSearchResponse {
    private Long courseId;
    private String title;
    private String profileImageUrl;
    private String nickname;
    private LocalDate createdDate;
    private CategoryResponse category;
    private List<TagResponse> tags;
    private List<CoursePlaceSearchResponse> coursePlaces;
    private double recommendation;
    private double difficulty;
    private int likeCount;
    private int viewCount;
    private boolean isLiked;

    public CourseDetailSearchResponse(Long courseId, String title, String profileImageUrl, String nickname, LocalDate createdDate,
                                      CategoryResponse category, List<TagResponse> tags, List<CoursePlaceSearchResponse> coursePlaces,
                                      double recommendation, double difficulty, int likeCount, int viewCount, boolean isLiked) {
        this.courseId = courseId;
        this.title = title;
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
        this.createdDate = createdDate;
        this.category = category;
        this.tags = tags;
        this.coursePlaces = coursePlaces;
        this.recommendation = recommendation;
        this.difficulty = difficulty;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.isLiked = isLiked;
    }
}
