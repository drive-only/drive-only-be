package drive_only.drive_only_server.dto.comment;

public record CommentCreateRequest(
        String content,
        Long parentId
) {
}
