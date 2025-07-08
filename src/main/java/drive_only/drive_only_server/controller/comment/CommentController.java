package drive_only.drive_only_server.controller.comment;

import drive_only.drive_only_server.dto.comment.create.CommentCreateRequest;
import drive_only.drive_only_server.dto.comment.create.CommentCreateResponse;
import drive_only.drive_only_server.dto.comment.delete.CommentDeleteResponse;
import drive_only.drive_only_server.dto.comment.search.CommentSearchListResponse;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateRequest;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateResponse;
import drive_only.drive_only_server.service.comment.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "댓글 조회", description = "댓글 및 대댓글들을 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 및 대댓글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 댓글을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })

    @GetMapping("/api/courses/{courseId}/comments")
    public ResponseEntity<CommentSearchListResponse> searchComments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        CommentSearchListResponse response = commentService.searchComments(courseId, page, size);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/api/courses/{courseId}/comments")
    public ResponseEntity<CommentCreateResponse> createComment(@PathVariable Long courseId, @RequestBody CommentCreateRequest request) {
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
    public ResponseEntity<CommentUpdateResponse> updateComment(@PathVariable Long commentId, @RequestBody CommentUpdateRequest request) {
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
}
