package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "hidden_comment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id","comment_id"}))
public class HiddenComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public HiddenComment(Member member, Comment comment) {
        this.member = member;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }
}
