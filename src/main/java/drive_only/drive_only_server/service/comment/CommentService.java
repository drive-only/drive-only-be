package drive_only.drive_only_server.service.comment;

import drive_only.drive_only_server.domain.Comment;
import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.dto.comment.create.CommentCreateRequest;
import drive_only.drive_only_server.dto.comment.create.CommentCreateResponse;
import drive_only.drive_only_server.repository.CommentRepository;
import drive_only.drive_only_server.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CourseRepository courseRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentCreateResponse createComment(Long courseId, CommentCreateRequest request) {
        Course course = findCourse(courseId);
        Comment comment = new Comment(request.content(), course, null);

        if (request.parentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
            parentComment.addChildComment(comment);
        }
        commentRepository.save(comment);
        return new CommentCreateResponse(comment.getId(), "댓글이 성공적으로 등록되었습니다.");
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스(게시글)을 찾을 수 없습니다."));
    }
}
