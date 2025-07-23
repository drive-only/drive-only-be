package drive_only.drive_only_server.service.comment;

import drive_only.drive_only_server.domain.Comment;
import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.comment.create.CommentCreateRequest;
import drive_only.drive_only_server.dto.comment.create.CommentCreateResponse;
import drive_only.drive_only_server.dto.comment.delete.CommentDeleteResponse;
import drive_only.drive_only_server.dto.comment.search.CommentSearchResponse;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateRequest;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateResponse;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.meta.Meta;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.custom.CommentNotFoundException;
import drive_only.drive_only_server.exception.custom.CourseNotFoundException;
import drive_only.drive_only_server.exception.custom.ParentCommentNotFoundException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.repository.comment.CommentRepository;
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.security.LoginMemberProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentService {
    private static final String SUCCESS_CREATE ="댓글이 성공적으로 등록되었습니다.";
    private static final String SUCCESS_UPDATE ="댓글이 성공적으로 수정되었습니다.";
    private static final String SUCCESS_DELETE ="댓글이 성공적으로 삭제되었습니다.";

    private final CourseRepository courseRepository;
    private final CommentRepository commentRepository;
    private final LoginMemberProvider loginMemberProvider;

    @Transactional
    public CommentCreateResponse createComment(Long courseId, CommentCreateRequest request) {
        Course course = findCourse(courseId);
        Member loginMember = loginMemberProvider.getLoginMember();
        Comment comment = Comment.createComment(request.content(), loginMember, course, null);

        if (request.parentCommentId() != null) {
            Comment parentComment = findParentComment(request.parentCommentId());
            parentComment.addChildComment(comment);
        }

        commentRepository.save(comment);

        return new CommentCreateResponse(comment.getId(), SUCCESS_CREATE);
    }

    public PaginatedResponse<CommentSearchResponse> searchComments(Long courseId, int page, int size) {
        Member loginMember = loginMemberProvider.getLoginMemberIfExists();
        Page<Comment> parentComments = commentRepository.findParentCommentsByCourseId(courseId, PageRequest.of(page, size));
        List<CommentSearchResponse> responses = parentComments.stream()
                .map(comment -> CommentSearchResponse.from(comment, loginMember))
                .toList();

        Meta meta = Meta.from(parentComments);

        return new PaginatedResponse<>(responses, meta);
    }

    @Transactional
    public CommentUpdateResponse updateComment(Long commentId, CommentUpdateRequest request) {
        Comment comment = findComment(commentId);
        comment.update(request);
        return new CommentUpdateResponse(comment.getId(), SUCCESS_UPDATE);
    }

    @Transactional
    public CommentDeleteResponse deleteComment(Long commentId) {
        Comment comment = findComment(commentId);
        comment.clearChildComments();
        commentRepository.delete(comment);
        return new CommentDeleteResponse(comment.getId(), SUCCESS_DELETE);
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId).orElseThrow(CourseNotFoundException::new);
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
    }

    private Comment findParentComment(Long parentCommentId) {
        return commentRepository.findById(parentCommentId).orElseThrow(ParentCommentNotFoundException::new);
    }
}
