package drive_only.drive_only_server.dto.course.create;

import drive_only.drive_only_server.dto.coursePlace.create.CoursePlaceCreateRequest;
import drive_only.drive_only_server.dto.tag.TagRequest;
import java.util.List;

public record CourseCreateRequest(
        String region,
        String subRegion,
        String time,
        String season,
        String theme,
        String areaType,
        String title,
        List<CoursePlaceCreateRequest> coursePlaces,
        List<TagRequest> tags,
        Double recommendation,
        Double difficulty,
        Boolean isPrivate
) {
}
