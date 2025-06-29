package drive_only.drive_only_server.repository;

import drive_only.drive_only_server.domain.SavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
}
