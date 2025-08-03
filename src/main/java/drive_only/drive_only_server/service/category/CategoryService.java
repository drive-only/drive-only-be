package drive_only.drive_only_server.service.category;

import drive_only.drive_only_server.domain.Category;
import drive_only.drive_only_server.dto.category.RegionResponse;
import drive_only.drive_only_server.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<RegionResponse> getRegionAndSubRegions() {
        List<Category> categories = categoryRepository.findAll();

        // Map<String, Set<String>>: 중복 방지를 위해 Set 사용
        Map<String, Set<String>> regionMap = new HashMap<>();

        for (Category category : categories) {
            String region = category.getRegion();
            String subRegion = category.getSubRegion();

            regionMap
                    .computeIfAbsent(region, k -> new LinkedHashSet<>()) // 순서 유지
                    .add(subRegion);
        }

        // Map → List<RegionResponse> 변환
        return regionMap.entrySet().stream()
                .map(entry -> new RegionResponse(
                        entry.getKey(),
                        new ArrayList<>(entry.getValue())
                ))
                .toList();
    }
}