package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "liked_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikedComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    public LikedComment(Member member, Comment comment) {
        this.member = member;
        this.comment = comment;
    }
}