package drive_only.drive_only_server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "created_date")
    private LocalDate createdDated;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    public Comment(String content, Course course, Comment parent) {
        this.content = content;
        this.course = course;
        this.parent = parent;
        this.createdDated = LocalDate.now();
        this.likeCount = 0;
        this.isDeleted = false;
    }
}
