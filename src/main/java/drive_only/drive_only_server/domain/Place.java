package drive_only.drive_only_server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    private String thumbNailUrl;
    private String useTime;
    private String restDate;
    private String phoneNum;

    protected Place() {
    }

    public Place(String name, String address, String thumbNailUrl, String useTime, String restDate, String phoneNum) {
        this.name = name;
        this.address = address;
        this.thumbNailUrl = thumbNailUrl;
        this.useTime = useTime;
        this.restDate = restDate;
        this.phoneNum = phoneNum;
    }
}
