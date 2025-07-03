package drive_only.drive_only_server.controller.comment;

import drive_only.drive_only_server.dto.comment.create.CommentCreateRequest;
import drive_only.drive_only_server.dto.comment.create.CommentCreateResponse;
import drive_only.drive_only_server.service.comment.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "댓글", description = "댓글 관련 API")
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록")
    @PostMapping("/api/courses/{courseId}/comments")
    public ResponseEntity<CommentCreateResponse> createComment(@PathVariable Long courseId, @RequestBody CommentCreateRequest request) {
        CommentCreateResponse response = commentService.createComment(courseId, request);
        return ResponseEntity.ok().body(response);
    }
}
