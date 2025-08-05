package drive_only.drive_only_server.service.photo;

import drive_only.drive_only_server.s3.S3ImageStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final S3ImageStorageProvider s3Provider;

    public String uploadImage(String imageData, String email) {
        return s3Provider.saveBase64Image(imageData, email);
    }
}