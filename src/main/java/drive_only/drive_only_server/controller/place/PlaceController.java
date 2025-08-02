package drive_only.drive_only_server.controller.place;

import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.place.myPlace.DeleteSavedPlaceResponse;
import drive_only.drive_only_server.dto.place.myPlace.SavePlaceResponse;
import drive_only.drive_only_server.dto.place.nearbySearch.NearbyPlacesResponse;
import drive_only.drive_only_server.dto.place.search.PlaceSearchRequest;
import drive_only.drive_only_server.dto.place.search.PlaceSearchResponse;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.service.place.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "장소", description = "장소 관련 API")
public class PlaceController {
    private final PlaceService placeService;

    @Operation(summary = "장소 리스트 조회", description = "조건에 따른 장소 리스트 조회")
    @ApiErrorCodeExamples({
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.KEYWORD_REQUIRED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/places")
    public ResponseEntity<PaginatedResponse<PlaceSearchResponse>> getPlaces(
            PlaceSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponse<PlaceSearchResponse> response = placeService.searchPlaces(request, page, size);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "주변 장소 리스트 조회", description = "분배 로직 반영하여 위치에 따른 주변 관광지/음식점 조회")
    @ApiErrorCodeExamples({
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/courses/{courseId}/nearby-places")
    public ResponseEntity<PaginatedResponse<NearbyPlacesResponse>> getNearbyPlaces(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "tourist-spot") String type
    ) {
        PaginatedResponse<NearbyPlacesResponse> response = placeService.searchNearbyPlaces(courseId, type);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "저장한 장소 리스트 조회", description = "사용자가 저장했던 장소들을 조회")
    @ApiErrorCodeExamples({
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/my-places")
    public ResponseEntity<PaginatedResponse<PlaceSearchResponse>> getSavedPlaces() {
        PaginatedResponse<PlaceSearchResponse> response = placeService.searchSavedPlaces();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "장소 저장", description = "사용자가 저장하고 싶은 장소 등록")
    @ApiErrorCodeExamples({
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/my-places/{placeId}")
    public ResponseEntity<SavePlaceResponse> savePlace(@PathVariable Long placeId) {
        SavePlaceResponse response = placeService.savePlace(placeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "저장한 장소 삭제", description = "사용자가 저장했던 장소를 삭제")
    @ApiErrorCodeExamples({
            ErrorCode.PLACE_NOT_FOUND,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @DeleteMapping("/api/my-places/{savedPlaceId}")
    public ResponseEntity<DeleteSavedPlaceResponse> deleteSavedPlace(@PathVariable Long savedPlaceId) {
        DeleteSavedPlaceResponse response = placeService.deleteSavedPlace(savedPlaceId);
        return ResponseEntity.ok().body(response);
    }
}
