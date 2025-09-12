package drive_only.drive_only_server.repository.place;

import static drive_only.drive_only_server.domain.QPlace.*;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.place.search.PlaceSearchRequest;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class PlaceRepositoryImpl implements PlaceRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public PlaceRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Place> searchPlaces(PlaceSearchRequest request, Pageable pageable) {
        List<Place> content = queryFactory
                .selectFrom(place)
                .where(
                        typeEq(request.type()),
                        keywordContains(request.keyword()),
                        regionEq(request.region()),
                        subRegionEq(request.subRegion())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(place.count())
                .from(place)
                .where(
                        typeEq(request.type()),
                        keywordContains(request.keyword()),
                        regionEq(request.region()),
                        subRegionEq(request.subRegion())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression typeEq(String type) {
        if (type == null) {
            return null;
        }
        if (type.equals("tourist-spots")) {
            return place.contentTypeId.in(12, 14, 38);
        }
        if (type.equals("restaurants")) {
            return place.contentTypeId.eq(39);
        }
        return null;
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword != null ? place.name.trim().contains(keyword) : null;
    }

    private BooleanExpression regionEq(String region) {
        return region != null ? place.region.eq(region) : null;
    }

    private BooleanExpression subRegionEq(String subRegion) {
        return subRegion != null ? place.subRegion.eq(subRegion) : null;
    }
}
