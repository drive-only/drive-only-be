package drive_only.drive_only_server.service.comment;

import drive_only.drive_only_server.domain.Comment;
import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.comment.create.CommentCreateRequest;
import drive_only.drive_only_server.dto.comment.create.CommentCreateResponse;
import drive_only.drive_only_server.dto.comment.search.CommentListResponse;
import drive_only.drive_only_server.dto.comment.search.CommentListResponse.Meta;
import drive_only.drive_only_server.dto.comment.search.CommentSearchResponse;
import drive_only.drive_only_server.repository.CommentRepository;
import drive_only.drive_only_server.repository.CourseRepository;
import drive_only.drive_only_server.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CourseRepository courseRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentCreateResponse createComment(Long courseId, CommentCreateRequest request) {
        Course course = findCourse(courseId);
        //TODO : 나중에 멤버 관련 기능들이 완성되면 리팩토링
        Member member = new Member("test", "test", "test", "test");
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
        //TODO : 나중에 멤버 관련 기능들이 완성되면 현재 로그인 된 멤버로 리팩토링
        Member member = new Member("test", "test", "test", "test");
        List<Comment> comments = course.getComments().stream()
                .filter(comment -> comment.getParentComment() == null)
                .toList();

        int total = comments.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<Comment> pagedComments = comments.subList(fromIndex, toIndex);
        List<CommentSearchResponse> rootResponses = pagedComments.stream()
                .map(comment -> createCommentResponse(comment, member))
                .toList();
        Boolean hasNext = toIndex < total;
        Meta meta = new Meta(total, page + 1, size, hasNext);

        return new CommentListResponse(rootResponses, meta);
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
