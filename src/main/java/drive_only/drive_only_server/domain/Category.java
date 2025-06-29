package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region")
    private String region;

    @Column(name = "sub_region")
    private String subRegion;

    @Column(name = "time")
    private String time;

    @Column(name = "season")
    private String season;

    @Column(name = "theme")
    private String theme;

    @Column(name = "area_type")
    private String areaType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    protected Category() {

    }

    public Category(Course course) {
        this.course = course;
    }
}
