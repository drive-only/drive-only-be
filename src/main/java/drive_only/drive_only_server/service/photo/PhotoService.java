package drive_only.drive_only_server.service.photo;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.s3.S3ImageStorageProvider;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final S3ImageStorageProvider s3;

    @Getter @AllArgsConstructor
    public static class UploadedPhoto {
        private final String key;     // 논리키(cp1_1 등)
        private final String s3Key;   // S3 object key
        private final String cdnUrl;  // CloudFront 조회 URL
    }

    public Map<String, UploadedPhoto> uploadManyInOrder(List<String> order, List<MultipartFile> files, String ownerEmail) {
        Map<String, S3ImageStorageProvider.UploadResult> raw = s3.uploadManyInOrder(order, files, ownerEmail);
        Map<String, UploadedPhoto> mapped = new LinkedHashMap<>();
        raw.forEach((logicalKey, ur) -> mapped.put(
                logicalKey, new UploadedPhoto(logicalKey, ur.getS3Key(), ur.getCdnUrl())
        ));
        return mapped;
    }

    public String uploadTemp(MultipartFile file, String ownerEmail) {
        return s3.uploadTemp(file, ownerEmail).getCdnUrl();
    }

    public String promoteIfTemp(String urlOrKey, Long memberId) {
        return s3.promoteTempToProfile(urlOrKey, memberId).getCdnUrl();
    }
}
