package drive_only.drive_only_server.dto.coursePlace;

import java.util.List;
import lombok.Getter;

@Getter
public class CoursePlaceCreateRequest {
    private String placeId;
    private String placeType;
    private String name;
    private String address;
    private String content;
    private List<String> photoUrls;
    private int sequence;

    public CoursePlaceCreateRequest(String placeId, String placeType, String name, String address, String content, List<String> photoUrls, int sequence) {
        this.placeId = placeId;
        this.placeType = placeType;
        this.name = name;
        this.address = address;
        this.content = content;
        this.photoUrls = photoUrls;
        this.sequence = sequence;
    }
}
