package drive_only.drive_only_server.controller.category;

import drive_only.drive_only_server.dto.category.RegionAndSubRegionResponse;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.service.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "카테고리", description = "지역/시군구 카테고리 조회 API")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "지역/시군구 목록 조회",
            description = "관광공사(Tour API) 정보를 이용해 지역과 하위 시·군·구 목록을 반환합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.TOUR_API_COMMUNICATION_FAILED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/categories")
    public Map<String, List<RegionAndSubRegionResponse>> getCategories() {
        List<RegionAndSubRegionResponse> regions = categoryService.getRegionAndSubRegions();
        return Map.of("region", regions);
    }
}