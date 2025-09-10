package drive_only.drive_only_server.domain;

import drive_only.drive_only_server.dto.course.create.CourseCreateRequest;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
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

    @Column(name = "is_private")
    private boolean isPrivate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    @Getter(AccessLevel.NONE)
    private List<LikedCourse> likedCourses = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CoursePlace> coursePlaces = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<Comment> comments = new ArrayList<>();

    public static Course createCourse(String title, LocalDate createdDate, Double recommendation, Double difficulty, int viewCount,
                                      int likeCount, int commentCount, boolean isReported, boolean isPrivate, Member member, Category category,
                                      List<CoursePlace> coursePlaces, List<Tag> tags) {
        validateTitle(title);
        validateRecommendation(recommendation);
        validateDifficulty(difficulty);
        validateCoursePlaces(coursePlaces);
        Course course = new Course();
        course.title = title;
        course.createdDate = createdDate;
        course.recommendation = recommendation;
        course.difficulty = difficulty;
        course.viewCount = viewCount;
        course.likeCount = likeCount;
        course.commentCount = commentCount;
        course.isReported = isReported;
        course.isPrivate = isPrivate;
        course.member = member;
        course.category = category;
        for (CoursePlace coursePlace : coursePlaces) {
            course.addCoursePlace(coursePlace);
        }
        for (Tag tag : tags) {
            course.addTag(tag);
        }
        return course;
    }

    public void update(CourseCreateRequest request, Category newCategory, List<CoursePlace> newCoursePlaces, List<Tag> newTags) {
        validateTitle(request.title());
        validateRecommendation(request.recommendation());
        validateDifficulty(request.difficulty());
        validateCoursePlaces(newCoursePlaces);

        this.title = request.title();
        this.recommendation = request.recommendation();
        this.difficulty = request.difficulty();
        this.category = newCategory;
        this.isPrivate = request.isPrivate();

        this.coursePlaces.clear();
        newCoursePlaces.forEach(this::addCoursePlace);

        this.tags.clear();
        newTags.forEach(this::addTag);
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 70) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_TITLE);
        }
    }

    private static void validateRecommendation(Double recommendation) {
        if (recommendation == null || recommendation < 0 || recommendation > 5) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_RECOMMENDATION);
        }
    }

    private static void validateDifficulty(Double difficulty) {
        if (difficulty == null || difficulty < 0 || difficulty > 5) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_DIFFICULTY);
        }
    }

    private static void validateCoursePlaces(List<CoursePlace> coursePlaces) {
        if (coursePlaces == null || coursePlaces.isEmpty()) {
            throw new BusinessException(ErrorCode.COURSE_PLACES_REQUIRED);
        }
    }

    public void addCoursePlace(CoursePlace coursePlace) {
        this.coursePlaces.add(coursePlace);
        coursePlace.setCourse(this);
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.setCourse(this);
    }

    public boolean isWrittenBy(Member loginMember) {
        return this.member.getId().equals(loginMember.getId());
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
