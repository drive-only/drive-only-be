package drive_only.drive_only_server.domain;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_place_id", nullable = false)
    private CoursePlace coursePlace;

    private Photo(String s3Key, String url) {
        this.s3Key = s3Key;
        this.url = url;
    }

    public static Photo create(String s3Key, String url) {
        if (s3Key == null || s3Key.isBlank() || url == null || url.isBlank()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
        String k = s3Key.trim();
        String u = url.trim();
        if (k.length() > 512 || u.length() > 2048) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
        return new Photo(k, u);
    }

    public void setS3KeyForMigrate(String s3Key) {
        this.s3Key = s3Key;
    }
}
