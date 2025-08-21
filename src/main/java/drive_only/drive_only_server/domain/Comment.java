package drive_only.drive_only_server.domain;

import drive_only.drive_only_server.dto.comment.update.CommentUpdateRequest;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> childComments = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<LikedComment> likedComments = new ArrayList<>();

    public static Comment createComment(String content, Member loginMember, Course course, Comment parentComment) {
        validateContent(content);
        
        Comment comment = new Comment();
        comment.content = content;
        comment.member = loginMember;
        comment.course = course;
        comment.parentComment = parentComment;
        comment.createdDate = LocalDateTime.now();
        comment.likeCount = 0;
        comment.isDeleted = false;
        return comment;
    }

    public void update(CommentUpdateRequest request) {
        validateContent(request.content());

        this.content = request.content();
        this.createdDate = LocalDateTime.now();
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank() || content.length() > 200) {
            throw new BusinessException(ErrorCode.INVALID_COMMENT_CONTENT);
        }
    }

    public void addChildComment(Comment child) {
        this.childComments.add(child);
        child.parentComment = this;
    }

    public void clearChildComments() {
        this.childComments.clear();
    }

    public boolean isWrittenBy(Member loginMember) {
        return this.member.getId().equals(loginMember.getId());
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

}
