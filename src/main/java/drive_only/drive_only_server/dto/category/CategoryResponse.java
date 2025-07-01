package drive_only.drive_only_server.dto.category;

public record CategoryResponse (
    String region,
    String subRegion,
    String season,
    String time,
    String theme,
    String areaType
) {}
