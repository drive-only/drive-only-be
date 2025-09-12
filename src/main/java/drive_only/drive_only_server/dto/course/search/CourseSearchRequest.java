package drive_only.drive_only_server.dto.course.search;

public record CourseSearchRequest(
        String keyword,
        String tag,
        Long placeId,
        Long memberId,
        String region,
        String subRegion,
        String time,
        String season,
        String theme,
        String areaType,
        String sort
) {}
