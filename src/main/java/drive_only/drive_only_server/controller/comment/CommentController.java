package drive_only.drive_only_server.controller.comment;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.comment.create.CommentCreateRequest;
import drive_only.drive_only_server.dto.comment.create.CommentCreateResponse;
import drive_only.drive_only_server.dto.comment.delete.CommentDeleteResponse;
import drive_only.drive_only_server.dto.comment.search.CommentSearchResponse;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateRequest;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateResponse;
import drive_only.drive_only_server.dto.common.ApiResult;
import drive_only.drive_only_server.dto.common.ApiResultSupport;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.like.comment.CommentLikeResponse;
import drive_only.drive_only_server.dto.report.ReportResponse;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.security.LoginMemberProvider;
import drive_only.drive_only_server.service.comment.CommentService;
import drive_only.drive_only_server.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Tag(name = "댓글", description = "댓글 관련 API")
public class CommentController {
    private final CommentService commentService;
    private final LoginMemberProvider loginMemberProvider;

    @Operation(summary = "댓글 및 대댓글 조회", description = "댓글과 대댓글을 조회")
    @ApiErrorCodeExamples({
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.PARENT_COMMENT_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/courses/{courseId}/comments")
    public ResponseEntity<ApiResult<PaginatedResponse<CommentSearchResponse>>> getComments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponse<CommentSearchResponse> result = commentService.searchComments(courseId, page, size);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_GET_COMMENTS, result);
    }

    @Operation(summary = "댓글 및 대댓글 등록", description = "새로운 댓글 또는 대댓글을 등록")
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_COMMENT_CONTENT,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/courses/{courseId}/comments")
    public ResponseEntity<ApiResult<CommentCreateResponse>> createComment(
            @PathVariable Long courseId,
            @RequestBody CommentCreateRequest request
    ) {
        CommentCreateResponse result = commentService.createComment(courseId, request);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_CREATE_COMMENT, result);
    }

    @Operation(summary = "댓글 및 대댓글 수정", description = "commentId를 이용해 기존 댓글 또는 대댓글을 수정")
    @ApiErrorCodeExamples({
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.INVALID_COMMENT_CONTENT,
            ErrorCode.OWNER_MISMATCH,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PatchMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResult<CommentUpdateResponse>> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        CommentUpdateResponse result = commentService.updateComment(commentId, request);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_UPDATE_COMMENT, result);
    }

    @Operation(summary = "댓글 및 대댓글 삭제", description = "commentId를 이용해 댓글 또는 대댓글을 삭제")
    @ApiErrorCodeExamples({
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.OWNER_MISMATCH,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResult<CommentDeleteResponse>> deleteComment(@PathVariable Long commentId) {
        CommentDeleteResponse result = commentService.deleteComment(commentId);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_DELETE_COMMENT, result);
    }

    @Operation(summary = "댓글 및 대댓글 좋아요 전송", description = "특정 댓글 또는 대댓글에 대해 좋아요 또는 좋아요 취소 요청을 처리합니다.")
    @ApiErrorCodeExamples({
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/comments/{commentId}/like")
    public ResponseEntity<ApiResult<CommentLikeResponse>> toggleLikeComment(
            @PathVariable Long commentId
    ) {
        Member member = loginMemberProvider.getLoginMember();

        CommentLikeResponse result = commentService.toggleCommentLike(commentId, member);
        HttpStatus status = result.liked() ? HttpStatus.CREATED : HttpStatus.OK;

        return ApiResultSupport.okWithStatus(
                SuccessCode.SUCCESS_TOGGLE_COMMENT_LIKE,
                status,
                result
        );
    }

    @Operation(summary = "댓글/대댓글 신고(숨김)", description = "해당 댓글을 신고하여 신고자 본인 화면에서만 숨깁니다.")
    @ApiErrorCodeExamples({
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/comments/{commentId}/report")
    public ResponseEntity<ApiResult<ReportResponse>> reportComment(@PathVariable Long commentId) {
        ReportResponse result = commentService.reportComment(commentId);
        SuccessCode sc = result.created()
                ? SuccessCode.SUCCESS_REPORT_COMMENT_CREATED
                : SuccessCode.SUCCESS_REPORT_COMMENT_ALREADY;
        return ApiResultSupport.ok(sc, result);
    }

    @Operation(summary = "댓글/대댓글 신고(숨김) 해제", description = "신고자 본인 화면에서 숨긴 댓글을 다시 보이도록 합니다.")
    @ApiErrorCodeExamples({
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @DeleteMapping("/api/comments/{commentId}/report")
    public ResponseEntity<ApiResult<ReportResponse>> unreportComment(@PathVariable Long commentId) {
        ReportResponse result = commentService.unreportComment(commentId);
        return ApiResultSupport.ok(SuccessCode.SUCCESS_UNREPORT_COMMENT, result);
    }
}
