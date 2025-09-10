package drive_only.drive_only_server.dto.like.course;

public record CourseLikeResponse(
        String message,
        int likeCount,
        boolean liked
) {
    public static CourseLikeResponse from(String message, int likeCount, boolean liked) {
        return new CourseLikeResponse(message, likeCount, liked);
    }
}

