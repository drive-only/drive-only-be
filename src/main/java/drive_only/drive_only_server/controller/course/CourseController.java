package drive_only.drive_only_server.controller.course;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.common.ApiResult;
import drive_only.drive_only_server.dto.common.ApiResultSupport;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.course.create.CourseCreateForm;
import drive_only.drive_only_server.dto.course.create.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.create.CourseCreateResponse;
import drive_only.drive_only_server.dto.course.delete.CourseDeleteResponse;
import drive_only.drive_only_server.dto.course.detailSearch.CourseDetailSearchResponse;
import drive_only.drive_only_server.dto.course.search.CourseSearchRequest;
import drive_only.drive_only_server.dto.course.search.CourseSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.update.CoursePlaceUpdateResponse;
import drive_only.drive_only_server.dto.like.course.CourseLikeResponse;
import drive_only.drive_only_server.dto.report.ReportResponse;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.security.LoginMemberProvider;
import drive_only.drive_only_server.service.course.CourseService;
import drive_only.drive_only_server.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "코스(게시글)", description = "드라이브 코스(게시글) 관련 API")
public class CourseController {
    private final CourseService courseService;
    private final LoginMemberProvider loginMemberProvider;

    @Operation(summary = "코스(게시글) 리스트 조회", description = "조건에 따른 드라이브 코스(게시글) 목록을 조회")
    @ApiErrorCodeExamples({
            ErrorCode.COURSE_NOT_FOUND,
            ErrorCode.PLACE_ID_WITH_ANYTHING_NOT_ALLOWED,
            ErrorCode.KEYWORD_WITH_CATEGORY_NOT_ALLOWED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/courses")
    public ResponseEntity<ApiResult<PaginatedResponse<CourseSearchResponse>>> getCourses(
            CourseSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        PaginatedResponse<CourseSearchResponse> result = courseService.searchCourses(request, page, size);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_GET_COURSES, result);
    }

    @Operation(summary = "코스(게시글) 상세 조회", description = "courseId를 이용하여 특정 드라이브 코스(게시글)의 상세 정보를 조회")
    @ApiErrorCodeExamples({
            ErrorCode.COURSE_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/courses/{courseId}")
    public ResponseEntity<ApiResult<PaginatedResponse<CourseDetailSearchResponse>>> searchCourseDetails(@PathVariable Long courseId) {
        PaginatedResponse<CourseDetailSearchResponse> result = courseService.searchCourseDetail(courseId);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_GET_COURSE_DETAIL, result);
    }

    @Operation(summary = "코스(게시글) 등록", description = "새로운 드라이브 코스(게시글)를 등록")
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_CATEGORY_REGION,
            ErrorCode.INVALID_CATEGORY_SEASON,
            ErrorCode.INVALID_CATEGORY_AREA_TYPE,
            ErrorCode.INVALID_COURSE_TITLE,
            ErrorCode.INVALID_COURSE_RECOMMENDATION,
            ErrorCode.INVALID_COURSE_DIFFICULTY,
            ErrorCode.INVALID_COURSE_PLACE_CONTENT,
            ErrorCode.INVALID_COURSE_PLACE_PHOTOS,
            ErrorCode.COURSE_PLACES_REQUIRED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping(value = "/api/courses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResult<CourseCreateResponse>> createCourse(
            @RequestPart("course") CourseCreateForm request,
            @RequestPart(name = "photos", required = false) List<MultipartFile> files
    ) {
        CourseCreateResponse result = courseService.createCourseFromMultipart(request, files);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_CREATE_COURSE, result);
    }

    @Operation(summary = "코스(게시글) 수정", description = "courseId를 이용해 기존 드라이브 코스(게시글)의 정보를 수정")
    @ApiErrorCodeExamples({
            ErrorCode.COURSE_NOT_FOUND,
            ErrorCode.INVALID_CATEGORY_REGION,
            ErrorCode.INVALID_CATEGORY_SEASON,
            ErrorCode.INVALID_CATEGORY_AREA_TYPE,
            ErrorCode.INVALID_COURSE_TITLE,
            ErrorCode.INVALID_COURSE_RECOMMENDATION,
            ErrorCode.INVALID_COURSE_DIFFICULTY,
            ErrorCode.INVALID_COURSE_PLACE_CONTENT,
            ErrorCode.INVALID_COURSE_PLACE_PHOTOS,
            ErrorCode.COURSE_PLACES_REQUIRED,
            ErrorCode.OWNER_MISMATCH,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PutMapping(value = "/api/courses/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResult<CoursePlaceUpdateResponse>> updateCourse(
            @PathVariable Long courseId,
            @RequestPart("course") CourseCreateForm form,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        CoursePlaceUpdateResponse result = courseService.updateCourseFromMultipart(courseId, form, photos);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_UPDATE_COURSE, result);
    }

    @Operation(summary = "코스(게시글) 삭제", description = "courseId를 이용해 드라이브 코스(게시글)를 삭제")
    @ApiErrorCodeExamples({
            ErrorCode.COURSE_NOT_FOUND,
            ErrorCode.OWNER_MISMATCH,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @DeleteMapping("/api/courses/{courseId}")
    public ResponseEntity<ApiResult<CourseDeleteResponse>> deleteCourse(@PathVariable Long courseId) {
        CourseDeleteResponse result = courseService.deleteCourse(courseId);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_DELETE_COURSE, result);
    }

    @Operation(summary = "코스(게시글) 좋아요 전송", description = "특정 코스에 대해 좋아요 또는 좋아요 취소 요청을 처리합니다.")
    @ApiErrorCodeExamples({
            ErrorCode.COURSE_NOT_FOUND,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/courses/{courseId}/like")
    public ResponseEntity<ApiResult<CourseLikeResponse>> toggleLikeCourse(
            @PathVariable Long courseId
    ) {
        Member member = loginMemberProvider.getLoginMember();

        CourseLikeResponse result = courseService.toggleCourseLike(courseId, member);
        HttpStatus status = result.liked() ? HttpStatus.CREATED : HttpStatus.OK;

        return ApiResultSupport.okWithStatus(
                SuccessCode.SUCCESS_TOGGLE_COURSE_LIKE,
                status,
                result
        );
    }

    @Operation(summary = "코스(게시글) 신고(숨김)", description = "해당 게시글을 신고하여 신고자 본인 화면에서만 숨깁니다.")
    @ApiErrorCodeExamples({
            ErrorCode.COURSE_NOT_FOUND,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/courses/{courseId}/report")
    public ResponseEntity<ApiResult<ReportResponse>> reportCourse(@PathVariable Long courseId) {
        ReportResponse result = courseService.reportCourse(courseId);
        SuccessCode sc = result.created()
                ? SuccessCode.SUCCESS_REPORT_COURSE_CREATED
                : SuccessCode.SUCCESS_REPORT_COURSE_ALREADY;
        return ApiResultSupport.ok(sc, result);
    }

    @Operation(summary = "코스(게시글) 신고(숨김) 해제", description = "신고자 본인 화면에서 숨긴 게시글을 다시 보이도록 합니다.")
    @ApiErrorCodeExamples({
            ErrorCode.COURSE_NOT_FOUND,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @DeleteMapping("/api/courses/{courseId}/report")
    public ResponseEntity<ApiResult<ReportResponse>> unreportCourse(@PathVariable Long courseId) {
        ReportResponse result = courseService.unreportCourse(courseId);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_UNREPORT_COURSE, result);
    }
}
