package drive_only.drive_only_server.dto.comment.create;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

public record CommentCreateRequest(
        @NotBlank
        String content,

        @Nullable
        Long parentCommentId
) {
}
