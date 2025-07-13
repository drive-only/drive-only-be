package drive_only.drive_only_server.controller.place;

import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.place.PlaceSearchRequest;
import drive_only.drive_only_server.dto.place.PlaceSearchResponse;
import drive_only.drive_only_server.service.place.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @GetMapping("/api/places")
    public ResponseEntity<PaginatedResponse<PlaceSearchResponse>> searchPlaces(
            PlaceSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponse<PlaceSearchResponse> response = placeService.searchPlaces(request, page, size);
        return ResponseEntity.ok().body(response);
    }

}
