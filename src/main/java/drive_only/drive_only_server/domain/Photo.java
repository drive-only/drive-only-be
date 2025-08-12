package drive_only.drive_only_server.domain;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_place_id", nullable = false)
    private CoursePlace coursePlace;

    private Photo(String url) { // 생성자 private → 정적 팩토리 강제
        this.url = url;
    }

    public static Photo create(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
        String normalized = url.trim();
        if (normalized.length() > 2048) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
        return new Photo(normalized);
    }

    public void setCoursePlace(CoursePlace coursePlace) {
        if (coursePlace == null) throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        this.coursePlace = coursePlace;
    }
}
