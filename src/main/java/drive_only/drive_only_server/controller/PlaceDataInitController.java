package drive_only.drive_only_server.controller;

import drive_only.drive_only_server.service.PlaceDataInitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlaceDataInitController {
    private final PlaceDataInitService placeDataInitService;

    public PlaceDataInitController(PlaceDataInitService placeDataInitService) {
        this.placeDataInitService = placeDataInitService;
    }

    @PostMapping("/api/init/places")
    public ResponseEntity<Void> initPlaceData() {
        placeDataInitService.importPlaceDataFromTourApi();
        return ResponseEntity.ok().build();
    }
}
