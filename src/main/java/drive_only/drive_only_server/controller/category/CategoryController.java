package drive_only.drive_only_server.controller.category;

import drive_only.drive_only_server.dto.category.RegionResponse;
import drive_only.drive_only_server.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/categories")
    public Map<String, List<RegionResponse>> getCategories() {
        List<RegionResponse> regions = categoryService.getRegionAndSubRegions();
        return Map.of("region", regions);
    }
}
