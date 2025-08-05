package drive_only.drive_only_server.dto.category;

import java.util.List;

public record RegionAndSubRegionResponse(
        String name,
        List<String> subRegion
) {
}
