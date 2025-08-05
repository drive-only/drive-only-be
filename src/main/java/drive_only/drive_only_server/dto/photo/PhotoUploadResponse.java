package drive_only.drive_only_server.dto.photo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PhotoUploadResponse {
    private String message;
    private String imageUrl;
}