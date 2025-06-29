package drive_only.drive_only_server.controller.course;

import drive_only.drive_only_server.dto.course.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.CourseCreateResponse;
import drive_only.drive_only_server.service.course.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/api/courses")
    public ResponseEntity<CourseCreateResponse> createCourse(@RequestBody CourseCreateRequest request) {
        CourseCreateResponse response = courseService.createCourse(request);
        return ResponseEntity.ok().body(response);
    }
}
