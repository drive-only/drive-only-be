package drive_only.drive_only_server.service.category;

import drive_only.drive_only_server.dto.category.RegionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final TourApiClient tourApiClient;

    public List<RegionResponse> getRegionAndSubRegions() {
        return tourApiClient.fetchRegionData();
    }
}
