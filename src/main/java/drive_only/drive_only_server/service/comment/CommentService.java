package drive_only.drive_only_server.service.comment;

import drive_only.drive_only_server.domain.Comment;
import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.comment.create.CommentCreateRequest;
import drive_only.drive_only_server.dto.comment.create.CommentCreateResponse;
import drive_only.drive_only_server.dto.comment.delete.CommentDeleteResponse;
import drive_only.drive_only_server.dto.comment.search.CommentListResponse;
import drive_only.drive_only_server.dto.comment.search.CommentListResponse.Meta;
import drive_only.drive_only_server.dto.comment.search.CommentSearchResponse;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateRequest;
import drive_only.drive_only_server.dto.comment.update.CommentUpdateResponse;
import drive_only.drive_only_server.repository.CommentRepository;
import drive_only.drive_only_server.repository.CourseRepository;
import drive_only.drive_only_server.repository.MemberRepository;
import java.util.List;
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
    private final MemberRepository memberRepository;

    @Transactional
    public CommentCreateResponse createComment(Long courseId, CommentCreateRequest request) {
        Course course = findCourse(courseId);
        //TODO : 나중에 멤버 관련 기능들이 완성되면 리팩토링 (현재 로그인 되어 있는 사용자로 리팩토링)
        Member member = Member.createMember("email", "nickname", "profile", ProviderType.KAKAO);
        memberRepository.save(member);
        Comment comment = new Comment(request.content(), member, course, null);

        if (request.parentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
            parentComment.addChildComment(comment);
        }
        commentRepository.save(comment);
        return new CommentCreateResponse(comment.getId(), "댓글이 성공적으로 등록되었습니다.");
    }

    public CommentListResponse searchComments(Long courseId, int page, int size) {
        Course course = findCourse(courseId);
        //TODO : 나중에 멤버 관련 기능들이 완성되면 현재 로그인 된 멤버로 리팩토링 (댓글 작성자로 리팩토링)
        Member member = Member.createMember("email", "nickname", "profile", ProviderType.KAKAO);
        List<Comment> comments = course.getComments().stream()
                .filter(comment -> comment.getParentComment() == null && !comment.isDeleted())
                .toList();

        int total = comments.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<Comment> pagedComments = comments.subList(fromIndex, toIndex);
        List<CommentSearchResponse> rootResponses = pagedComments.stream()
                .map(comment -> createCommentResponse(comment, member))
                .toList();

        Boolean hasNext = parentComments.hasNext();
        int total = (int) parentComments.getTotalElements();
        Meta meta = new Meta(total, page + 1, size, hasNext);

        return new CommentListResponse(responses, meta);
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
        return new CommentSearchResponse(
                comment.getId(),
                comment.getMember().getId(),
                comment.getMember().getNickname(),
                comment.getContent(),
                comment.getCreatedDate(), // LocalDate -> LocalDateTime 변환 참고
                comment.getLikeCount(),
                loginMember != null && comment.getMember().equals(loginMember),
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
