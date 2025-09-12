package drive_only.drive_only_server.repository.course;

import static drive_only.drive_only_server.domain.QCategory.category;
import static drive_only.drive_only_server.domain.QCourse.course;
import static drive_only.drive_only_server.domain.QMember.member;
import static drive_only.drive_only_server.domain.QCoursePlace.coursePlace;
import static drive_only.drive_only_server.domain.QPlace.place;
import static drive_only.drive_only_server.domain.QHiddenCourse.hiddenCourse;
import static drive_only.drive_only_server.domain.QTag.tag;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import drive_only.drive_only_server.domain.Course;

import drive_only.drive_only_server.dto.course.search.CourseSearchRequest;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.data.domain.*;
public class CourseRepositoryImpl implements CourseRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public CourseRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Course> searchCourses(CourseSearchRequest request, Pageable pageable, Long viewerId) {
        BooleanExpression notHiddenByViewer = viewerId != null
                ? JPAExpressions.selectOne().from(hiddenCourse)
                .where(hiddenCourse.course.eq(course)
                        .and(hiddenCourse.member.id.eq(viewerId)))
                .notExists()
                : null;

        List<Course> content = queryFactory
                .selectFrom(course)
                .join(course.member, member).fetchJoin()
                .join(course.category, category).fetchJoin()
                .join(course.coursePlaces, coursePlace)
                .join(coursePlace.place, place)
                .join(course.tags, tag)
                .where(
                        keywordContains(request.keyword()),
                        tagContains(request.tag()),
                        placeEq(request.placeId()),
                        memberEq(request.memberId()),
                        regionEq(request.region()),
                        subRegionEq(request.subRegion()),
                        timeEq(request.time()),
                        seasonEq(request.season()),
                        themeEq(request.theme()),
                        areaTypeEq(request.areaType()),
                        notHiddenByViewer
                )
                .distinct()
                .orderBy(getSortMethod(request))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(course.countDistinct())
                .from(course)
                .join(course.member, member)
                .join(course.category, category)
                .join(course.coursePlaces, coursePlace)
                .join(coursePlace.place, place)
                .join(course.tags, tag)
                .where(
                        keywordContains(request.keyword()),
                        placeEq(request.placeId()),
                        regionEq(request.region()),
                        subRegionEq(request.subRegion()),
                        timeEq(request.time()),
                        seasonEq(request.season()),
                        themeEq(request.theme()),
                        areaTypeEq(request.areaType()),
                        notHiddenByViewer
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public List<Course> findCoursesByMember(Long memberId, Long lastId, int size) {
        return queryFactory
                .selectFrom(course)
                .join(course.member, member).fetchJoin()
                .join(course.category, category).fetchJoin()
                .leftJoin(course.coursePlaces, coursePlace)
                .where(
                        course.member.id.eq(memberId),
                        lastId != null ? course.id.lt(lastId) : null
                )
                .distinct()
                .orderBy(course.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword != null ? course.title.contains(keyword) : null;
    }

    private BooleanExpression tagContains(String tagName) {
        return tagName != null ? tag.name.trim().contains(tagName) : null;
    }

    private BooleanExpression placeEq(Long placeId) {
        return placeId != null ? coursePlace.place.id.eq(placeId) : null;
    }

    private BooleanExpression memberEq(Long memberId) {
        return memberId != null ? course.member.id.eq(memberId) : null;
    }

    private BooleanExpression regionEq(String region) {
        return region != null ? course.category.region.eq(region) : null;
    }

    private BooleanExpression subRegionEq(String subRegion) {
        return subRegion != null ? course.category.subRegion.eq(subRegion) : null;
    }

    private BooleanExpression timeEq(String time) {
        return time != null ? course.category.time.eq(time) : null;
    }

    private BooleanExpression seasonEq(String season) {
        return season != null ? course.category.season.eq(season) : null;
    }

    private BooleanExpression themeEq(String theme) {
        return theme != null ? course.category.theme.eq(theme) : null;
    }

    private BooleanExpression areaTypeEq(String areaType) {
        return areaType != null ? course.category.areaType.eq(areaType) : null;
    }

    private OrderSpecifier<?>[] getSortMethod(CourseSearchRequest request) {
        String sort = request.sort();

        if (sort == null || sort.equals("latest")) {
            return new OrderSpecifier<?>[]{
                    course.createdDate.desc(),
                    course.id.desc()
            };
        }
        if (sort.equals("likeCount")) {
            return new OrderSpecifier<?>[]{
                    course.likeCount.desc(),
                    course.id.desc()
            };
        }

        return new OrderSpecifier<?>[]{
                course.createdDate.desc(),
                course.id.desc()
        };
    }
}
