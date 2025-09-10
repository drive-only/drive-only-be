package drive_only.drive_only_server.migrate;

import drive_only.drive_only_server.domain.Photo;
import drive_only.drive_only_server.repository.photo.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("migrate")
public class BackfillPhotoS3Key implements CommandLineRunner {
    private final PhotoRepository photoRepository;

    @Override
    public void run(String... args) {
        List<Photo> photos = photoRepository.findAll();
        for (Photo p : photos) {
            if (p.getS3Key() != null && !p.getS3Key().isBlank()) continue;
            String url = p.getUrl();
            if (url == null) continue;
            String s3Key = extractKey(url);
            if (s3Key != null) {
                p.setS3KeyForMigrate(s3Key);
            }
        }
        photoRepository.saveAll(photos);
    }

    private String extractKey(String url) {
        try {
            java.net.URI u = java.net.URI.create(url);
            String path = u.getPath(); // "/uploads/..../file.jpg"
            if (path == null || path.isBlank()) return null;
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            return null;
        }
    }
}

