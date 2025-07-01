package drive_only.drive_only_server.dto.course.create;

import drive_only.drive_only_server.dto.coursePlace.create.CoursePlaceCreateRequest;
import drive_only.drive_only_server.dto.tag.TagRequest;
import drive_only.drive_only_server.dto.tag.TagResponse;
import java.util.List;
import lombok.Getter;

@Getter
public class CourseCreateRequest {
    //카테고리
    private String region;
    private String subRegion;
    private String time;
    private String season;
    private String theme;
    private String areaType;
    private String title;
    private List<CoursePlaceCreateRequest> coursePlaces;
    private List<TagRequest> tags;
    private Double recommendation;
    private Double difficulty;
    private boolean isPrivate;

    public CourseCreateRequest(String region, String subRegion, String time, String season, String theme, String areaType,
                               String title, List<CoursePlaceCreateRequest> coursePlaces, List<TagRequest> tags, Double recommendation,
                               Double difficulty, boolean isPrivate) {
        this.region = region;
        this.subRegion = subRegion;
        this.time = time;
        this.season = season;
        this.theme = theme;
        this.areaType = areaType;
        this.title = title;
        this.coursePlaces = coursePlaces;
        this.tags = tags;
        this.recommendation = recommendation;
        this.difficulty = difficulty;
        this.isPrivate = isPrivate;
    }
}
