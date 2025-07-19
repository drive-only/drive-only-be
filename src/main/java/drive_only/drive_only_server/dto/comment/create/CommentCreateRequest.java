package drive_only.drive_only_server.dto.comment.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @Size(min = 1, max = 200, message = "내용은 1자 이상 200자 이하로 입력해주세요.")
        @NotBlank(message = "내용을 입력해주세요.")
        String content,
        Long parentCommentId
) {
}
