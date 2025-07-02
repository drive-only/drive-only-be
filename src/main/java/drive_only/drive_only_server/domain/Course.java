package drive_only.drive_only_server.domain;

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

    @Column(name = "is_liked")
    private boolean isLiked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private List<LikedCourse> likedCourses = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private List<CoursePlace> coursePlaces = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<Tag> tags = new ArrayList<>();

    public static Course createCourse(String title, LocalDate createdDate, Double recommendation, Double difficulty, int viewCount,
                                      int likeCount, int commentCount, boolean isReported, Member member, Category category,
                                      List<CoursePlace> coursePlaces, List<Tag> tags) {
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
        course.setCategory(category);
        for (CoursePlace coursePlace : coursePlaces) {
            course.addCoursePlace(coursePlace);
        }
        for (Tag tag : tags) {
            course.addTag(tag);
        }
        return course;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void addCoursePlace(CoursePlace coursePlace) {
        coursePlaces.add(coursePlace);
        coursePlace.setCourse(this);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.setCourse(this);
    }

    public void addLikedCourse(LikedCourse likedCourse) {
        likedCourses.add(likedCourse);
        likedCourse.setCourse(this);
    }
}
