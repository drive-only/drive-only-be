package drive_only.drive_only_server.controller.data;

import drive_only.drive_only_server.service.data.PlaceDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlaceDataController {
    private final PlaceDataService placeDataService;

    @PostMapping("/api/init/places")
    public ResponseEntity<Void> initPlaceData() {
        placeDataService.importPlaceDataFromTourApi();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/sync/places")
    public ResponseEntity<Void> syncPlacesData() {
        placeDataService.syncPlaceDataFromTourApi();
        return ResponseEntity.ok().build();
    }
}
