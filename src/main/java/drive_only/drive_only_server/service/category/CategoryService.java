package drive_only.drive_only_server.service.category;

import drive_only.drive_only_server.dto.category.RegionAndSubRegionResponse;
import drive_only.drive_only_server.dto.data.AreaCodeResponse.Item;
import drive_only.drive_only_server.service.client.TourApiClient;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final TourApiClient tourApiClient;

    public List<RegionAndSubRegionResponse> getRegionAndSubRegions() {
        List<RegionAndSubRegionResponse> results = new ArrayList<>();

        List<Item> regions = tourApiClient.fetchAreaCodes(null);
        for (Item region : regions) {
            List<Item> subRegions = tourApiClient.fetchAreaCodes(region.code());
            List<String> subRegionList = new ArrayList<>();
            for (Item subRegion : subRegions) {
                subRegionList.add(subRegion.name());
            }
            results.add(new RegionAndSubRegionResponse(region.name(), subRegionList));
        }

        return results;
    }
}
