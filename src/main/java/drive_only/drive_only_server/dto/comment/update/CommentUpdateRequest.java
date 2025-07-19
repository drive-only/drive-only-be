package drive_only.drive_only_server.dto.comment.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @Size(min = 1, max = 200, message = "최소 1자 이상, 200자 이하의 숫자를 입력하세요.")
        @NotBlank(message = "내용을 입력해주세요.")
        String content
) {
}
