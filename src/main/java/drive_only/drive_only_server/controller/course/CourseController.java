package drive_only.drive_only_server.controller.course;

import drive_only.drive_only_server.dto.course.create.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.create.CourseCreateResponse;
import drive_only.drive_only_server.dto.course.delete.CourseDeleteResponse;
import drive_only.drive_only_server.dto.course.detailSearch.CourseDetailSearchResponse;
import drive_only.drive_only_server.dto.course.search.CourseSearchListResponse;
import drive_only.drive_only_server.dto.course.search.CourseSearchRequest;
import drive_only.drive_only_server.dto.course.search.CourseSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.update.CoursePlaceUpdateResponse;
import drive_only.drive_only_server.service.course.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "코스(게시글)", description = "드라이브 코스(게시글) 관련 API")
public class CourseController {
    private final CourseService courseService;

    @Operation(summary = "코스(게시글) 등록", description = "새로운 드라이브 코스(게시글)를 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코스가 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/api/courses")
    public ResponseEntity<CourseCreateResponse> createCourse(@RequestBody CourseCreateRequest request) {
        CourseCreateResponse response = courseService.createCourse(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "코스(게시글) 리스트 조회", description = "조건에 따른 드라이브 코스(게시글) 목록을 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코스 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/courses")
    public ResponseEntity<CourseSearchListResponse> searchCourses(
            CourseSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        CourseSearchListResponse response = courseService.searchCourses(request, page, size);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "코스(게시글) 상세 조회", description = "courseId를 이용하여 특정 드라이브 코스(게시글)의 상세 정보를 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코스 상세 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 코스(게시글)을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/courses/{courseId}")
    public ResponseEntity<CourseDetailSearchResponse> searchCourseDetail(@PathVariable Long courseId) {
        CourseDetailSearchResponse response = courseService.searchCourseDetail(courseId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "코스(게시글) 수정", description = "courseId를 이용해 기존 드라이브 코스(게시글)의 정보를 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코스 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 코스(게시글)을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PutMapping("/api/courses/{courseId}")
    public ResponseEntity<CoursePlaceUpdateResponse> updateCourse(@PathVariable Long courseId, @RequestBody CourseCreateRequest request) {
        CoursePlaceUpdateResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "코스(게시글) 삭제", description = "courseId를 이용해 드라이브 코스(게시글)를 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코스 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 코스(게시글)을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @DeleteMapping("/api/courses/{courseId}")
    public ResponseEntity<CourseDeleteResponse> deleteCourse(@PathVariable Long courseId) {
        CourseDeleteResponse response = courseService.deleteCourse(courseId);
        return ResponseEntity.ok().body(response);
    }
}
