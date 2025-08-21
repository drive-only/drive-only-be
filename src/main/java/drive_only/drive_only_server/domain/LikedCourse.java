package drive_only.drive_only_server.domain;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(
        name = "liked_course",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "course_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikedCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public LikedCourse(Member member, Course course) {
        if (member == null) throw new BusinessException(ErrorCode.UNAUTHENTICATED_MEMBER);
        if (course == null) throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        this.member = member;
        this.course = course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
