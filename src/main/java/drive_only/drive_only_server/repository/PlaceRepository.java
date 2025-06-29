package drive_only.drive_only_server.repository;

import drive_only.drive_only_server.domain.Place;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findByContentId(int contentId);
}
