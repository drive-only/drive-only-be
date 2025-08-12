package drive_only.drive_only_server.domain;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "liked_comment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "comment_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikedComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    public LikedComment(Member member, Comment comment) {
        if (member == null) throw new BusinessException(ErrorCode.UNAUTHENTICATED_MEMBER);
        if (comment == null) throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        this.member = member;
        this.comment = comment;
        // 양방향 유지: Member가 likedComments 컬렉션을 가짐
        member.addLikedComment(this);
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
