package drive_only.drive_only_server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Getter;

@Entity
@Getter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private LocalDate createdDate;
    private double recommendation;
    private double difficulty;
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

    protected Course() {
    }

    public Course(String title, LocalDate createdDate, double recommendation, double difficulty, int viewCount, int likeCount, int commentCount, boolean isReported) {
        this.title = title;
        this.createdDate = createdDate;
        this.recommendation = recommendation;
        this.difficulty = difficulty;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isReported = isReported;
    }
}
