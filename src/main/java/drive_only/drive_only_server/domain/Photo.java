package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo")
    private String photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_place_id")
    private CoursePlace coursePlace;

    protected Photo() {

    }

    public Photo(CoursePlace coursePlace) {
        this.coursePlace = coursePlace;
    }
}
