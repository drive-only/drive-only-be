package drive_only.drive_only_server.repository;

import drive_only.drive_only_server.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberReporitory extends JpaRepository<Member, Long> {
}
