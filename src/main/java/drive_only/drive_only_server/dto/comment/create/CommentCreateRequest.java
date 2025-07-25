package drive_only.drive_only_server.dto.comment.create;

public record CommentCreateRequest(
        String content,
        Long parentCommentId
) {
}
