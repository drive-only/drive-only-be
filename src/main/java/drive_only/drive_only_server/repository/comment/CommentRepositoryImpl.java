package drive_only.drive_only_server.repository.comment;

import static drive_only.drive_only_server.domain.QComment.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import drive_only.drive_only_server.domain.Comment;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class CommentRepositoryImpl implements CommentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public CommentRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Comment> findParentCommentsByCourseId(Long courseId, Pageable pageable) {
        List<Comment> content = queryFactory
                .selectFrom(comment)
                .distinct()
                .join(comment.member).fetchJoin()
                .where(
                        comment.course.id.eq(courseId),
                        comment.parentComment.isNull()
                )
                .orderBy(comment.createdDate.desc(), comment.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.course.id.eq(courseId),
                        comment.parentComment.isNull()
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
