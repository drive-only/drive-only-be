package drive_only.drive_only_server.controller.comment;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.comment.create.CommentCreateRequest;
import drive_only.drive_only_server.dto.comment.create.CommentCreateResponse;
import drive_only.drive_only_server.dto.comment.delete.CommentDeleteResponse;
import drive_only.drive_only_server.dto.comment.search.CommentSearchResponse;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateRequest;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateResponse;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.like.comment.CommentLikeResponse;
import drive_only.drive_only_server.exception.ErrorResponse;
import drive_only.drive_only_server.service.Member.MemberService;
import drive_only.drive_only_server.service.comment.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.security.core.Authentication;
import drive_only.drive_only_server.security.CustomUserPrincipal;


@RestController
@RequiredArgsConstructor
@Tag(name = "댓글", description = "댓글 관련 API")
public class CommentController {
    private final CommentService commentService;
    private final MemberService memberService;

    @Operation(summary = "댓글 조회", description = "댓글 및 대댓글들을 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 및 대댓글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 댓글을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/courses/{courseId}/comments")
    public ResponseEntity<PaginatedResponse<CommentSearchResponse>> getComments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponse<CommentSearchResponse> response = commentService.searchComments(courseId, page, size);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "코스를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/courses/{courseId}/comments")
    public ResponseEntity<CommentCreateResponse> createComment(
            @PathVariable Long courseId,
            @RequestBody CommentCreateRequest request
    ) {
        CommentCreateResponse response = commentService.createComment(courseId, request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "댓글 수정", description = "commentId를 이용해 기존 댓글을 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 댓글을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PatchMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentUpdateResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        CommentUpdateResponse response = commentService.updateComment(commentId, request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "댓글 삭제", description = "commentId를 이용해 댓글을 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 댓글을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentDeleteResponse> deleteComment(@PathVariable Long commentId) {
        CommentDeleteResponse response = commentService.deleteComment(commentId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "댓글 좋아요 전송", description = "특정 댓글에 대해 좋아요 또는 좋아요 취소 요청을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
            @ApiResponse(responseCode = "201", description = "좋아요 등록 성공"),
            @ApiResponse(responseCode = "404", description = "해당 댓글을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/api/comments/{commentId}/like")
    public ResponseEntity<CommentLikeResponse> toggleLikeComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String email = principal.getEmail();
        ProviderType provider = principal.getProvider();
        Member member = memberService.findByEmailAndProvider(email, provider);

        CommentLikeResponse response = commentService.toggleCommentLike(commentId, member);
        HttpStatus status = response.liked() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

}
