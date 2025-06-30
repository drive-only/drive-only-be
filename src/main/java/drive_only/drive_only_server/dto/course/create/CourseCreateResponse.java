package drive_only.drive_only_server.dto.course;

import lombok.Getter;

@Getter
public class CourseCreateResponse {
    private Long id;
    private String message;

    public CourseCreateResponse(Long id, String message) {
        this.id = id;
        this.message = message;
    }
}
