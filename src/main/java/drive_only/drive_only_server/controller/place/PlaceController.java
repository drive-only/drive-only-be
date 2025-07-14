package drive_only.drive_only_server.controller.place;

import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.place.NearbyPlacesResponse;
import drive_only.drive_only_server.dto.place.PlaceSearchRequest;
import drive_only.drive_only_server.dto.place.PlaceSearchResponse;
import drive_only.drive_only_server.service.place.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
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

    @GetMapping("/api/courses/{courseId}/nearby-places")
    public ResponseEntity<PaginatedResponse<NearbyPlacesResponse>> searchNearbyPlaces(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "tourist-spot") String type
    ) {
        PaginatedResponse<NearbyPlacesResponse> response = placeService.searchNearbyPlaces(courseId, type);
        return ResponseEntity.ok().body(response);
    }
}
