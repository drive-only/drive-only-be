package drive_only.drive_only_server.dto.like.comment;

public record CommentLikeResponse(
        String message,
        int likeCount,
        boolean liked
) {
    public static CommentLikeResponse from(String message, int likeCount, boolean liked) {
        return new CommentLikeResponse(message, likeCount, liked);
    }
}