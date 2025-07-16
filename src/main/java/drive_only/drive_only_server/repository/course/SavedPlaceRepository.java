package drive_only.drive_only_server.repository.course;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.SavedPlace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    List<SavedPlace> findByMember(Member member);
}
