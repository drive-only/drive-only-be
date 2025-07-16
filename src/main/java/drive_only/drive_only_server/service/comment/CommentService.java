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
import drive_only.drive_only_server.repository.comment.CommentRepository;
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.security.LoginMemberProvider;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentService {
    private final CourseRepository courseRepository;
    private final CommentRepository commentRepository;
    private final LoginMemberProvider loginMemberProvider;

    @Transactional
    public CommentCreateResponse createComment(Long courseId, CommentCreateRequest request) {
        Course course = findCourse(courseId);
        Member loginMember = loginMemberProvider.getLoginMember()
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다."));
        Comment comment = new Comment(request.content(), loginMember, course, null);

        if (request.parentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
            parentComment.addChildComment(comment);
        }
        commentRepository.save(comment);

        return new CommentCreateResponse(comment.getId(), "댓글이 성공적으로 등록되었습니다.");
    }

    public PaginatedResponse<CommentSearchResponse> searchComments(Long courseId, int page, int size) {
        Optional<Member> loginMember = loginMemberProvider.getLoginMember();
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> parentComments = commentRepository.findParentCommentsByCourseId(courseId, pageable);
        List<CommentSearchResponse> responses = parentComments.stream()
                .map(comment -> createCommentResponse(comment, loginMember.orElse(null)))
                .toList();

        Boolean hasNext = parentComments.hasNext();
        int total = (int) parentComments.getTotalElements();
        Meta meta = new Meta(total, page + 1, size, hasNext);

        return new PaginatedResponse<>(responses, meta);
    }

    @Transactional
    public CommentUpdateResponse updateComment(Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
        comment.update(request);
        return new CommentUpdateResponse(comment.getId(), "댓글이 성공적으로 수정되었습니다.");
    }

    @Transactional
    public CommentDeleteResponse deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
        if (comment.getChildComments() != null) {
            comment.getChildComments().clear();
        }
        commentRepository.delete(comment);
        return new CommentDeleteResponse(comment.getId(), "댓글이 성공적으로 삭제되었습니다.");
    }

    private CommentSearchResponse createCommentResponse(Comment comment, Member loginMember) {
        boolean isMine = loginMember != null && comment.getMember().equals(loginMember);
        return new CommentSearchResponse(
                comment.getId(),
                comment.getMember().getId(),
                comment.getMember().getNickname(),
                comment.getContent(),
                comment.getCreatedDate(), // LocalDate -> LocalDateTime 변환 참고
                comment.getLikeCount(),
                isMine,
                comment.isDeleted(),
                comment.getChildComments().stream()
                        .map(child -> createCommentResponse(child, loginMember))
                        .toList()
        );
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스(게시글)을 찾을 수 없습니다."));
    }
}
