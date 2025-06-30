package drive_only.drive_only_server.dto.coursePlace;

import java.util.List;
import lombok.Getter;

@Getter
public class CoursePlaceSearchResponse {
    private Long coursePlaceId;
    private Long placeId;
    private String placeType;
    private String name;
    private String address;
    private String content;
    private List<String> photoUrls;
    private double lat;
    private double lng;
    private int sequence;

    public CoursePlaceSearchResponse(Long coursePlaceId, Long placeId, String placeType, String name, String address, String content, List<String> photoUrls, double lat, double lng, int sequence) {
        this.coursePlaceId = coursePlaceId;
        this.placeId = placeId;
        this.placeType = placeType;
        this.name = name;
        this.address = address;
        this.content = content;
        this.photoUrls = photoUrls;
        this.lat = lat;
        this.lng = lng;
        this.sequence = sequence;
    }
}
