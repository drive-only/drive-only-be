package drive_only.drive_only_server.repository.comment;

import drive_only.drive_only_server.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    // 기존 메서드는 Custom에서 대체하므로 아래 라인은 삭제 또는 사용 중지 권장
    // Page<Comment> findParentCommentsByCourseId(Long courseId, Long viewerId, Pageable pageable);
}
