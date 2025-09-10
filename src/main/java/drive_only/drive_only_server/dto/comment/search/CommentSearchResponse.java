package drive_only.drive_only_server.dto.comment.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import drive_only.drive_only_server.domain.Comment;
import drive_only.drive_only_server.domain.Member;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CommentSearchResponse(
        Long commentId,
        Long memberId,
        String profileImageUrl,
        String nickname,
        String content,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
        int likeCount,
        Boolean isMine,
        Boolean isLiked,
        Boolean isDeleted,
        List<CommentSearchResponse> replies
) {
    public static CommentSearchResponse from(Comment comment, Member loginMember) {
        return new CommentSearchResponse(
                comment.getId(),
                comment.getMember().getId(),
                comment.getMember().getProfileImageUrl(),
                comment.getMember().getNickname(),
                comment.getContent(),
                comment.getCreatedDate(),
                comment.getLikeCount(),
                isMine(comment, loginMember),
                isLiked(comment, loginMember),
                comment.isDeleted(),
                getReplies(comment, loginMember)
        );
    }

    public static CommentSearchResponse fromFiltered(Comment comment, Member loginMember, java.util.Set<Long> hiddenIds) {
        boolean isMine = comment.getMember().equals(loginMember);
        boolean isLiked = loginMember.getLikedComments().stream()
                .anyMatch(likedComment -> likedComment.getComment().getId().equals(comment.getId()));

        List<CommentSearchResponse> replies = comment.getChildComments().stream()
                .filter(child -> !hiddenIds.contains(child.getId()))
                .map(child -> CommentSearchResponse.fromFiltered(child, loginMember, hiddenIds))
                .toList();

        return new CommentSearchResponse(
                comment.getId(),
                comment.getMember().getId(),
                comment.getMember().getProfileImageUrl(),
                comment.getMember().getNickname(),
                comment.getContent(),
                comment.getCreatedDate(),
                comment.getLikeCount(),
                isMine,
                isLiked,
                comment.isDeleted(),
                replies
        );
    }

    private static boolean isMine(Comment comment, Member loginMember) {
        return loginMember != null && comment.isWrittenBy(loginMember);
    }

    private static boolean isLiked(Comment comment, Member loginMember) {
        boolean isLiked = false;
        if (loginMember != null) {
            isLiked = loginMember.getLikedComments().stream()
                    .anyMatch(lc -> lc.getComment().getId().equals(comment.getId()));
        }
        return isLiked;
    }

    private static List<CommentSearchResponse> getReplies(Comment comment, Member loginMember) {
        return comment.getChildComments().stream()
                .map(child -> CommentSearchResponse.from(child, loginMember))
                .toList();
    }
}
