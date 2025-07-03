package drive_only.drive_only_server.dto.comment.search;

import java.time.LocalDateTime;
import java.util.List;

public record CommentSearchResponse(
        Long commentId,
        Long memberId,
        String nickname,
        String content,
        LocalDateTime createdDate,
        int likeCount,
        Boolean isMine,
        Boolean isDeleted,
        List<CommentSearchResponse> replies
) {
}
