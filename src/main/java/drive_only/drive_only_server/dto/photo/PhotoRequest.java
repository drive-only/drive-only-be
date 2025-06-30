package drive_only.drive_only_server.dto.photo;

import lombok.Getter;

@Getter
public class PhotoRequest {
    private String photoUrl;

    public PhotoRequest(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
