package drive_only.drive_only_server.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;


@Component
@RequiredArgsConstructor
public class S3ImageStorageProvider {

    private final AmazonS3 amazonS3;
    @Value("${s3.bucket}")
    private String bucketName;

    public String saveBase64Image(String base64Data, String email) {
        try {
            String[] parts = base64Data.split(",");
            String metadata = parts[0]; // data:image/png;base64
            String data = parts[1];

            String extension = metadata.contains("png") ? "png" : "jpg";
            byte[] decoded = Base64.getDecoder().decode(data);

            String fileName = String.format("uploads/%s/%s.%s",
                    LocalDate.now(), UUID.randomUUID(), extension);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(decoded.length);
            objectMetadata.setContentType("image/" + extension);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);

            amazonS3.putObject(bucketName, fileName, inputStream, objectMetadata);

            return amazonS3.getUrl(bucketName, fileName).toString();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    public String saveMultipartFile(MultipartFile file, String ownerEmail) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);
        }

        // 확장자/콘텐트타입 결정
        String original = file.getOriginalFilename();
        String ext = null;
        if (original != null && original.lastIndexOf('.') != -1) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        if (ext == null || ext.isBlank()) {
            String ct = file.getContentType(); // e.g. image/jpeg
            if (ct != null && ct.startsWith("image/")) {
                ext = ct.substring("image/".length()).toLowerCase(); // jpeg, png, webp, gif...
            }
        }
        // 확장자 정규화 및 허용 타입 체크
        if ("jpeg".equals(ext)) ext = "jpg";
        if (ext == null || !(ext.equals("jpg") || ext.equals("png") || ext.equals("webp") || ext.equals("gif"))) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);
        }

        // S3 object key (이메일/날짜 기준으로 디렉토리 구성)
        String owner = (ownerEmail == null || ownerEmail.isBlank()) ? "anonymous" : ownerEmail;
        String key = String.format("uploads/%s/%s/%s.%s",
                owner, LocalDate.now(), UUID.randomUUID(), ext);

        // 메타데이터
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentType("image/" + ("jpg".equals(ext) ? "jpeg" : ext));

        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(bucketName, key, in, meta);
            return amazonS3.getUrl(bucketName, key).toString();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }
}

