package drive_only.drive_only_server.repository.course;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.SavedPlace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    @Query("select sp from SavedPlace sp join fetch sp.place where sp.member = :member")
    List<SavedPlace> findByMember(Member member);
}
