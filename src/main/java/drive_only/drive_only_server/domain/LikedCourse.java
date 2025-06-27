package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
public class LikedCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    protected LikedCourse() {

    }

    public LikedCourse(Member member, Course course) {
        this.member = member;
        this.course = course;
    }
}
