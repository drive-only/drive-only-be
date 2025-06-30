package drive_only.drive_only_server.dto.photo;

import lombok.Getter;

@Getter
public class PhotoResponse {
    private Long photoId;
    private String photoUrl;

    public PhotoResponse(Long photoId, String photoUrl) {
        this.photoId = photoId;
        this.photoUrl = photoUrl;
    }
}
