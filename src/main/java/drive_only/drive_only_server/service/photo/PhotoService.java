package drive_only.drive_only_server.service.photo;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.s3.S3ImageStorageProvider;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final S3ImageStorageProvider s3Provider;

    // data URL 대략 검증 (필요 최소)
    private static final Pattern DATA_URL_REGEX =
            Pattern.compile("^data:image/(png|jpeg|jpg|webp|gif);base64,.+", Pattern.CASE_INSENSITIVE);

    public String uploadImage(String imageData, String email) {
        if (imageData == null || imageData.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);
        }
        String data = imageData.trim();
        if (!DATA_URL_REGEX.matcher(data).matches()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);
        }

        try {
            return s3Provider.saveBase64Image(data, email);
        } catch (RuntimeException ex) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    public String uploadFile(MultipartFile file, String email) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);
        }
        try {
            return s3Provider.saveMultipartFile(file, email);
        } catch (RuntimeException ex) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }
}
