package drive_only.drive_only_server.repository.hide;

import drive_only.drive_only_server.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface HiddenCommentRepository extends JpaRepository<HiddenComment, Long> {
    boolean existsByCommentAndMember(Comment comment, Member member);
    void deleteByCommentAndMember(Comment comment, Member member);

    @Query("select hc.comment.id from HiddenComment hc where hc.member.id = :memberId")
    Set<Long> findHiddenCommentIdsByMemberId(@Param("memberId") Long memberId);
}
