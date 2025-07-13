package drive_only.drive_only_server.repository.place;

import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.place.PlaceSearchRequest;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long>, PlaceRepositoryCustom {
    Optional<Place> findByContentId(int contentId);
    Page<Place> searchPlaces(PlaceSearchRequest request, Pageable pageable);
}
