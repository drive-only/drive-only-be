package drive_only.drive_only_server.repository.impl;

import static drive_only.drive_only_server.domain.QCategory.category;
import static drive_only.drive_only_server.domain.QCourse.*;
import static drive_only.drive_only_server.domain.QMember.member;
import static drive_only.drive_only_server.domain.QCoursePlace.coursePlace;
import static drive_only.drive_only_server.domain.QPlace.place;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.dto.course.search.CourseSearchRequest;
import drive_only.drive_only_server.repository.custom.CourseRepositoryCustom;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class CourseRepositoryImpl implements CourseRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public CourseRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Course> searchCourses(CourseSearchRequest request, Pageable pageable) {
        List<Course> content = queryFactory
                .selectFrom(course)
                .join(course.member, member).fetchJoin()
                .join(course.category, category).fetchJoin()
                .join(course.coursePlaces, coursePlace)
                .join(coursePlace.place, place)
                .where(
                        keywordContains(request.keyword()),
                        placeEq(request.placeId()),
                        regionEq(request.region()),
                        subRegionEq(request.subRegion()),
                        timeEq(request.time()),
                        seasonEq(request.season()),
                        themeEq(request.theme()),
                        areaTypeEq(request.areaType())
                )
                .distinct()
                .orderBy(getSortMethod(request))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(course.countDistinct())
                .from(course)
                .join(course.member, member).fetchJoin()
                .join(course.category, category).fetchJoin()
                .join(course.coursePlaces, coursePlace)
                .join(coursePlace.place, place)
                .where(
                        keywordContains(request.keyword()),
                        placeEq(request.placeId()),
                        regionEq(request.region()),
                        subRegionEq(request.subRegion()),
                        timeEq(request.time()),
                        seasonEq(request.season()),
                        themeEq(request.theme()),
                        areaTypeEq(request.areaType())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword != null ? course.title.contains(keyword) : null;
    }

    private BooleanExpression placeEq(Long placeId) {
        return placeId != null ? coursePlace.place.id.eq(placeId) : null;
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
        return season != null ? course.category.time.eq(season) : null;
    }

    private BooleanExpression themeEq(String theme) {
        return theme != null ? course.category.time.eq(theme) : null;
    }

    private BooleanExpression areaTypeEq(String areaType) {
        return areaType != null ? course.category.time.eq(areaType) : null;
    }

    private OrderSpecifier<?> getSortMethod(CourseSearchRequest request) {
        String sort = request.sort();

        if (sort == null || sort.equals("latest")) {
            return course.createdDate.desc();
        }
        if (sort.equals("likeCount")) {
            return course.likeCount.desc();
        }
        if (sort.equals("viewCount")) {
            return course.viewCount.desc();
        }

        return course.createdDate.desc();
    }
}
