package drive_only.drive_only_server.dto.category;

import lombok.Getter;

@Getter
public class CategoryResponse {
    private String region;
    private String subRegion;
    private String season;
    private String time;
    private String theme;
    private String areaType;

    public CategoryResponse(String region, String subRegion, String season, String time, String theme, String areaType) {
        this.region = region;
        this.subRegion = subRegion;
        this.season = season;
        this.time = time;
        this.theme = theme;
        this.areaType = areaType;
    }
}
