package drive_only.drive_only_server.controller.course;

import drive_only.drive_only_server.dto.course.create.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.create.CourseCreateResponse;
import drive_only.drive_only_server.dto.course.delete.CourseDeleteResponse;
import drive_only.drive_only_server.dto.course.detailSearch.CourseDetailSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.update.CoursePlaceUpdateResponse;
import drive_only.drive_only_server.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @PostMapping("/api/courses")
    public ResponseEntity<CourseCreateResponse> createCourse(@RequestBody CourseCreateRequest request) {
        CourseCreateResponse response = courseService.createCourse(request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/api/courses/{courseId}")
    public ResponseEntity<CourseDetailSearchResponse> searchCourseDetail(@PathVariable Long courseId) {
        CourseDetailSearchResponse response = courseService.searchCourseDetail(courseId);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/api/courses/{courseId}")
    public ResponseEntity<CoursePlaceUpdateResponse> updateCourse(@PathVariable Long courseId, CourseCreateRequest request) {
        CoursePlaceUpdateResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/api/courses/{courseId}")
    public ResponseEntity<CourseDeleteResponse> deleteCourse(@PathVariable Long courseId) {
        CourseDeleteResponse response = courseService.deleteCourse(courseId);
        return ResponseEntity.ok().body(response);
    }
}
