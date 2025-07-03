package drive_only.drive_only_server.controller.data;

import drive_only.drive_only_server.service.data.PlaceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "데이터 관리", description = "장소 데이터 초기화 및 동기화 관련 API")
public class PlaceDataController {
    private final PlaceDataService placeDataService;

    @Operation(summary = "장소 데이터 초기화", description = "한국관광공사 TourAPI로부터 장소 데이터를 최초로 불러와 저장(초기 세팅용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 장소 데이터가 초기화 됨"),
            @ApiResponse(responseCode = "500", description = "서버 오류 (TourAPI 문제 등)")
    })
    @PostMapping("/api/init/places")
    public ResponseEntity<Void> initPlaceData() {
        placeDataService.importPlaceDataFromTourApi();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "장소 데이터 동기화", description = "한국관광공사 TourAPI로부터 장소 데이터를 동기화")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 장소 데이터가 동기화 됨"),
            @ApiResponse(responseCode = "500", description = "서버 오류 (TourAPI 문제 등)")
    })
    @PostMapping("/api/sync/places")
    public ResponseEntity<Void> syncPlacesData() {
        placeDataService.syncPlaceDataFromTourApi();
        return ResponseEntity.ok().build();
    }
}
