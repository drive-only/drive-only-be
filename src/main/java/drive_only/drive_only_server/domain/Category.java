package drive_only.drive_only_server.domain;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region")
    private String region;

    @Column(name = "sub_region")
    private String subRegion;

    @Column(name = "time")
    private String time;

    @Column(name = "season")
    private String season;

    @Column(name = "theme")
    private String theme;

    @Column(name = "area_type")
    private String areaType;

    public Category(String region, String subRegion, String time, String season, String theme, String areaType) {
        this.region = region;
        this.subRegion = subRegion;
        this.time = time;
        this.season = season;
        this.theme = theme;
        this.areaType = areaType;
    }
}
