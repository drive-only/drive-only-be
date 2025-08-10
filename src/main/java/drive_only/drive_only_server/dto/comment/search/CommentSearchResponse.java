package drive_only.drive_only_server.dto.comment.search;

import drive_only.drive_only_server.domain.Comment;
import drive_only.drive_only_server.domain.Member;
import java.time.LocalDateTime;
import java.util.List;

public record CommentSearchResponse(
        Long commentId,
        Long memberId,
        String profileImageUrl,
        String nickname,
        String content,
        LocalDateTime createdDate,
        int likeCount,
        Boolean isMine,
        Boolean isLiked,
        Boolean isDeleted,
        List<CommentSearchResponse> replies
) {
    public static CommentSearchResponse from(Comment comment, Member loginMember) {
        boolean isMine = comment.getMember().equals(loginMember);
        boolean isLiked = loginMember.getLikedComments().stream()
                .anyMatch(likedComment -> likedComment.getComment().getId().equals(comment.getId()));

        List<CommentSearchResponse> replies = comment.getChildComments().stream()
                .map(child -> CommentSearchResponse.from(child, loginMember))
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
}
