package drive_only.drive_only_server.dto.comment;

import java.time.LocalDateTime;

public record CommentCreateResponse(
        Long commentId,
        Long parentCommentId,
        String comment,
        String nickname,
        LocalDateTime createdDate,
        
) {
}
