package drive_only.drive_only_server.repository.place;

import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.place.PlaceSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlaceRepositoryCustom {
    Page<Place> searchPlaces(PlaceSearchRequest request, Pageable pageable);
}
