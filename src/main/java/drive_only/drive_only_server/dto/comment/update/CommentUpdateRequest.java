package drive_only.drive_only_server.dto.comment.update;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
        @NotBlank
        String content
) {
}
