package drive_only.drive_only_server.service.comment;

import drive_only.drive_only_server.domain.Comment;
import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.LikedComment;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.comment.create.CommentCreateRequest;
import drive_only.drive_only_server.dto.comment.create.CommentCreateResponse;
import drive_only.drive_only_server.dto.comment.delete.CommentDeleteResponse;
import drive_only.drive_only_server.dto.comment.search.CommentSearchResponse;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateRequest;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateResponse;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.like.comment.CommentLikeResponse;
import drive_only.drive_only_server.dto.meta.Meta;
import drive_only.drive_only_server.exception.custom.CommentNotFoundException;
import drive_only.drive_only_server.exception.custom.CourseNotFoundException;
import drive_only.drive_only_server.exception.custom.OwnerMismatchException;
import drive_only.drive_only_server.exception.custom.ParentCommentNotFoundException;
import drive_only.drive_only_server.repository.comment.CommentRepository;
import drive_only.drive_only_server.repository.comment.LikedCommentRepository;
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

    private final CourseRepository courseRepository;
    private final CommentRepository commentRepository;
    private final LikedCommentRepository likedCommentRepository;
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
        return new CommentCreateResponse(comment.getId());
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
        validateCommentOwner(comment);
        comment.update(request);
        return new CommentUpdateResponse(comment.getId());
    }

    @Transactional
    public CommentDeleteResponse deleteComment(Long commentId) {
        Comment comment = findComment(commentId);
        validateCommentOwner(comment);
        comment.clearChildComments();
        commentRepository.delete(comment);
        return new CommentDeleteResponse(comment.getId());
    }

    @Transactional
    public CommentLikeResponse toggleCommentLike(Long commentId, Member loginMember) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
        boolean isLiked = likedCommentRepository.existsByCommentAndMember(comment, loginMember);

        if (isLiked) {
            likedCommentRepository.deleteByCommentAndMember(comment, loginMember);
            comment.decreaseLikeCount();
            return CommentLikeResponse.from("댓글의 좋아요를 취소 하였습니다.", comment.getLikeCount(), false);
        } else {
            likedCommentRepository.save(new LikedComment(loginMember, comment));
            comment.increaseLikeCount();
            return CommentLikeResponse.from("댓글에 좋아요를 눌렀습니다.", comment.getLikeCount(), true);
        }
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

    private void validateCommentOwner(Comment comment) {
        Member loginMember = loginMemberProvider.getLoginMember();
        if (!comment.isWrittenBy(loginMember)) {
            throw new OwnerMismatchException();
        }
    }
}
