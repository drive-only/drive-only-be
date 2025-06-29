package drive_only.drive_only_server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "recommendation")
    private Double recommendation;

    @Column(name = "difficulty")
    private Double difficulty;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "comment_count")
    private int commentCount;

    @Column(name = "is_reported")
    private boolean isReported;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "course")
    private List<LikedCourse> likedCourses = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<CoursePlace> coursePlaces = new ArrayList<>();

    public static Course createCourse(String title, LocalDate createdDate, Double recommendation, Double difficulty, int viewCount,
                                      int likeCount, int commentCount, boolean isReported, Member member, List<CoursePlace> coursePlaces) {
        Course course = new Course();
        course.title = title;
        course.createdDate = createdDate;
        course.recommendation = recommendation;
        course.difficulty = difficulty;
        course.viewCount = viewCount;
        course.likeCount = likeCount;
        course.commentCount = commentCount;
        course.isReported = isReported;
        course.setMember(member);
        for (CoursePlace coursePlace : coursePlaces) {
            course.addCoursePlace(coursePlace);
        }
        return course;
    }

    public void addCoursePlace(CoursePlace coursePlace) {
        coursePlaces.add(coursePlace);
        coursePlace.setCourse(this);
    }

    public void addLikedCourse(LikedCourse likedCourse) {
        likedCourses.add(likedCourse);
        likedCourse.setCourse(this);
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
