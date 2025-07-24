package drive_only.drive_only_server.dto.course.create;

import drive_only.drive_only_server.dto.coursePlace.create.CoursePlaceCreateRequest;
import drive_only.drive_only_server.dto.tag.TagRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CourseCreateRequest(
    @NotBlank
    String region,

    String subRegion,
    String time,

    @NotBlank
    String season,

    String theme,

    @NotBlank
    String areaType,

    @NotBlank
    String title,

    @NotEmpty
    List<CoursePlaceCreateRequest> coursePlaces,

    List<TagRequest> tags,

    @NotNull
    Double recommendation,

    Double difficulty,
    Boolean isPrivate
) {}
