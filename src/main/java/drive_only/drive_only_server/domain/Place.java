package drive_only.drive_only_server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id")
    private int contentId;

    @Column(name = "content_type_id")
    private int contentTypeId;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "thumbnail_url")
    private String thumbNailUrl;

    @Column(name = "use_time")
    private String useTime;

    @Column(name = "rest_date")
    private String restDate;

    @Column(name = "phone_num")
    private String phoneNum;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    public Place(int contentId, int contentTypeId, String name, String address, String thumbNailUrl, String useTime, String restDate, String phoneNum, Double lat, Double lng) {
        this.contentId = contentId;
        this.contentTypeId = contentTypeId;
        this.name = name;
        this.address = address;
        this.thumbNailUrl = thumbNailUrl;
        this.useTime = useTime;
        this.restDate = restDate;
        this.phoneNum = phoneNum;
        this.lat = lat;
        this.lng = lng;
    }
}
