package drive_only.drive_only_server.service.category;

import drive_only.drive_only_server.dto.category.RegionAndSubRegionResponse;
import drive_only.drive_only_server.dto.data.tourapi.AreaCodeResponse.Item;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
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
        try {
            List<RegionAndSubRegionResponse> results = new ArrayList<>();

            List<Item> regions = tourApiClient.fetchAreaCodes(null);
            if (regions == null || regions.isEmpty()) {
                throw new BusinessException(ErrorCode.TOUR_API_COMMUNICATION_FAILED);
            }

            for (Item region : regions) {
                List<Item> subRegions = tourApiClient.fetchAreaCodes(region.code());
                List<String> subRegionList = new ArrayList<>();
                if (subRegions != null) {
                    for (Item subRegion : subRegions) {
                        subRegionList.add(subRegion.name());
                    }
                }
                results.add(new RegionAndSubRegionResponse(region.name(), subRegionList));
            }

            return results;
        } catch (BusinessException e) {
            // 클라이언트에서 올라온 전용 코드 그대로 전파
            throw e;
        } catch (RuntimeException e) {
            // 기타 예외 → 통신 실패로 표준화
            throw new BusinessException(ErrorCode.TOUR_API_COMMUNICATION_FAILED);
        }
    }
}
