package drive_only.drive_only_server.repository.comment;

import drive_only.drive_only_server.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {
    Page<Comment> findParentCommentsByCourseId(Long courseId, Pageable pageable);
}
