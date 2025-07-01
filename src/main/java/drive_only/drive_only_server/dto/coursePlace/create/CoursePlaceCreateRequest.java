package drive_only.drive_only_server.dto.coursePlace.create;

import drive_only.drive_only_server.domain.Photo;
import drive_only.drive_only_server.dto.photo.PhotoRequest;
import java.util.List;
import lombok.Getter;

@Getter
public class CoursePlaceCreateRequest {
    private String placeId;
    private String placeType;
    private String name;
    private String address;
    private String content;
    private List<PhotoRequest> photoUrls;
    private int sequence;

    public CoursePlaceCreateRequest(String placeId, String placeType, String name, String address, String content, List<PhotoRequest> photoUrls, int sequence) {
        this.placeId = placeId;
        this.placeType = placeType;
        this.name = name;
        this.address = address;
        this.content = content;
        this.photoUrls = photoUrls;
        this.sequence = sequence;
    }
}
