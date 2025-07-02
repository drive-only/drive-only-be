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
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "course_place")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoursePlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "place_type")
    private String placeType;

    @Column(name = "content")
    private String content;

    @Column(name = "sequence")
    private int sequence;

    @OneToMany(mappedBy = "coursePlace", cascade = CascadeType.REMOVE)
    private List<Photo> photos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    public CoursePlace(String name, String placeType, String content, List<Photo> photos, int sequence, Place place) {
        this.name = name;
        this.placeType = placeType;
        this.content = content;
        if (photos != null) {
            for (Photo photo : photos) {
                addPhoto(photo);
            }
        }
        this.sequence = sequence;
        this.place = place;
    }

    public void setCourse(Course course) {
        this.course = course;
        if (!course.getCoursePlaces().contains(this)) {
            course.getCoursePlaces().add(this);
        }
    }

    public void addPhoto(Photo photo) {
        this.photos.add(photo);
        photo.setCoursePlace(this);
    }
}
