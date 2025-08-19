package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "hidden_course",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id","course_id"}))
public class HiddenCourse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public HiddenCourse(Member member, Course course) {
        this.member = member;
        this.course = course;
        this.createdAt = LocalDateTime.now();
    }
}
