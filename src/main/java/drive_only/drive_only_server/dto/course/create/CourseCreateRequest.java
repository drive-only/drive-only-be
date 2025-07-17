package drive_only.drive_only_server.dto.course.create;

import drive_only.drive_only_server.dto.coursePlace.create.CoursePlaceCreateRequest;
import drive_only.drive_only_server.dto.tag.TagRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CourseCreateRequest(
    @NotBlank(message = "지역을 선택해주세요.")
    String region,

    String subRegion,
    String time,

    @NotBlank(message = "계절을 선택해주세요.")
    String season,

    String theme,

    @NotBlank(message = "지역 유형을 선택해주세요.")
    String areaType,

    @Size(min = 1, max = 70, message = "제목은 1자 이상 70자 이하로 입력해주세요.")
    @NotBlank(message = "제목을 입력해주세요.")
    String title,

    @NotEmpty(message = "코스 장소를 1개 이상 등록해주세요.")
    List<CoursePlaceCreateRequest> coursePlaces,

    List<TagRequest> tags,

    @NotNull(message = "추천도를 입력해주세요.")
    Double recommendation,

    Double difficulty,
    Boolean isPrivate
) {}
