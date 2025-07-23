package drive_only.drive_only_server.dto.comment.create;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank(message = "내용을 입력해주세요.")
        String content,
        Long parentCommentId
) {
}
