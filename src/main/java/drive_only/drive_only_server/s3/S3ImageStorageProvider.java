package drive_only.drive_only_server.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;


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
}

