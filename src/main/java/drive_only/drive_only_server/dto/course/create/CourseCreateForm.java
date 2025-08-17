package drive_only.drive_only_server.dto.course.create;

import drive_only.drive_only_server.dto.tag.TagRequest;
import java.util.List;

public record CourseCreateForm(
        String region,
        String subRegion,
        String time,
        String season,
        String theme,
        String areaType,
        String title,
        List<CoursePlaceDraft> coursePlaces,
        List<TagRequest> tags,
        Double recommendation,
        Double difficulty,
        Boolean isPrivate,
        List<String> photoKeyOrder
) {
}
