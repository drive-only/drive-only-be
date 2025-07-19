package drive_only.drive_only_server.dto.category;

import drive_only.drive_only_server.domain.Course;

public record CategoryResponse (
    String region,
    String subRegion,
    String season,
    String time,
    String theme,
    String areaType
) {
    public static CategoryResponse from(Course course) {
        return new CategoryResponse(
                course.getCategory().getRegion(),
                course.getCategory().getSubRegion(),
                course.getCategory().getSeason(),
                course.getCategory().getTime(),
                course.getCategory().getTheme(),
                course.getCategory().getAreaType()
        );
    }
}
