package drive_only.drive_only_server.repository.course;

import static drive_only.drive_only_server.domain.QLikedCourse.likedCourse;
import static drive_only.drive_only_server.domain.QCourse.course;
import static drive_only.drive_only_server.domain.QMember.member;
import static drive_only.drive_only_server.domain.QCategory.category;
import static drive_only.drive_only_server.domain.QCoursePlace.coursePlace;

import com.querydsl.jpa.impl.JPAQueryFactory;
import drive_only.drive_only_server.domain.LikedCourse;
import drive_only.drive_only_server.domain.Member;
import jakarta.persistence.EntityManager;
import java.util.List;

public class LikedCourseRepositoryImpl implements LikedCourseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public LikedCourseRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<LikedCourse> findLikedCoursesByMember(Member loginUser, Long lastId, int size) {
        return queryFactory
                .selectFrom(likedCourse)
                .join(likedCourse.course, course).fetchJoin()
                .join(course.member, member).fetchJoin()
                .join(course.category, category).fetchJoin()
                .leftJoin(course.coursePlaces, coursePlace).fetchJoin() // 장소까지 미리 로딩
                .where(
                        likedCourse.member.eq(loginUser),
                        lastIdCondition(lastId)
                )
                .distinct()
                .orderBy(course.id.desc())
                .limit(size)
                .fetch();
    }

    private com.querydsl.core.types.dsl.BooleanExpression lastIdCondition(Long lastId) {
        return lastId != null ? course.id.lt(lastId) : null;
    }
}
