package drive_only.drive_only_server.controller.init;

import drive_only.drive_only_server.service.init.PlaceDataInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlaceDataInitController {
    private final PlaceDataInitService placeDataInitService;

    @PostMapping("/api/init/places")
    public ResponseEntity<Void> initPlaceData() {
        placeDataInitService.importPlaceDataFromTourApi();
        return ResponseEntity.ok().build();
    }
}
