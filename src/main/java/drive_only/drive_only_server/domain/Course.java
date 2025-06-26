package drive_only.drive_only_server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Entity
@Getter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private LocalDate createdDate;
    private Double recommendation;
    private Double difficulty;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private boolean isReported;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liked_course_id")
    private LikedCourse likedCourse;

    @OneToMany(mappedBy = "course")
    private List<CoursePlace> coursePlaces;

    protected Course() {
    }

    public Course(String title, Member member, LikedCourse likedCourse) {
        this.title = title;
        this.member = member;
        this.likedCourse = likedCourse;
        this.createdDate = LocalDate.now();
        this.recommendation = 0.0;
        this.difficulty = 0.0;
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.isReported = false;
    }
}
