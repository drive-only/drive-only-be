package drive_only.drive_only_server.domain;

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

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "place_type")
    private String placeType;

    @Column(name = "content")
    private String content;

    @Column(name = "sequence")
    private int sequence;

    @OneToMany(mappedBy = "coursePlace", cascade = CascadeType.REMOVE)
    private List<Photo> photos = new ArrayList<>();

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    public static CoursePlace createCoursePlace(String placeName, String placeType, String content, List<Photo> photos, int sequence, Place place) {
        validateContent(content);
        validatePhotos(photos);
        CoursePlace coursePlace = new CoursePlace();
        coursePlace.placeName = placeName;
        coursePlace.placeType = placeType;
        coursePlace.content = content;
        if (!photos.isEmpty()) {
            for (Photo photo : photos) {
                coursePlace.addPhoto(photo);
            }
        }
        coursePlace.sequence = sequence;
        coursePlace.place = place;
        return coursePlace;
    }

    private static void validateContent(String content) {
        if (content == null) {
            content = "";
        }
        if (content.length() > 500) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_PLACE_CONTENT);
        }
    }

    private static void validatePhotos(List<Photo> photos) {
        if (photos.size() > 5) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_PLACE_PHOTOS);
        }
    }

    private void addPhoto(Photo photo) {
        this.photos.add(photo);
        photo.setCoursePlace(this);
    }
}
