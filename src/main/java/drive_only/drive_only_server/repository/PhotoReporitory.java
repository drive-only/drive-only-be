package drive_only.drive_only_server.repository;

import drive_only.drive_only_server.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoReporitory extends JpaRepository<Photo, Long> {
}
