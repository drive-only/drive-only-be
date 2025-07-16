package drive_only.drive_only_server.controller.place;

import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.place.nearbySearch.NearbyPlacesResponse;
import drive_only.drive_only_server.dto.place.search.PlaceSearchRequest;
import drive_only.drive_only_server.dto.place.search.PlaceSearchResponse;
import drive_only.drive_only_server.service.place.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "장소", description = "장소 관련 API")
public class PlaceController {
    private final PlaceService placeService;

    @Operation(summary = "장소 리스트 조회", description = "조건에 따른 장소 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/places")
    public ResponseEntity<PaginatedResponse<PlaceSearchResponse>> searchPlaces(
            PlaceSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponse<PlaceSearchResponse> response = placeService.searchPlaces(request, page, size);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "주변 장소 리스트 조회", description = "분배 로직 반영하여 위치에 따른 주변 관광지/음식점 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주변 장소 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/courses/{courseId}/nearby-places")
    public ResponseEntity<PaginatedResponse<NearbyPlacesResponse>> searchNearbyPlaces(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "tourist-spot") String type
    ) {
        PaginatedResponse<NearbyPlacesResponse> response = placeService.searchNearbyPlaces(courseId, type);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "저장한 장소 리스트 조회", description = "사용자가 저장했던 장소들을 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장한 장소 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/my-places")
    public ResponseEntity<PaginatedResponse<PlaceSearchResponse>> searchSavedPlaces() {
        PaginatedResponse<PlaceSearchResponse> response = placeService.searchSavedPlaces();
        return ResponseEntity.ok().body(response);
    }
}
