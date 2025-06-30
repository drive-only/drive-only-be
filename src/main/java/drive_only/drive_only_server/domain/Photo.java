package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url")
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_place_id")
    private CoursePlace coursePlace;

    public Photo(String url) {
        this.url = url;
    }

    public void setCoursePlace(CoursePlace coursePlace) {
        this.coursePlace = coursePlace;
    }
}
