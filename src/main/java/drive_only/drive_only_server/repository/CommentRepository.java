package drive_only.drive_only_server.repository;

import drive_only.drive_only_server.domain.Comment;
import drive_only.drive_only_server.repository.custom.CommentRepositoryCustom;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    Page<Comment> findParentCommentsByCourseId(Long courseId, Pageable pageable);
}
