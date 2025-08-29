package drive_only.drive_only_server.repository.course;

import static drive_only.drive_only_server.domain.QSavedPlace.savedPlace;
import static drive_only.drive_only_server.domain.QPlace.place;

import com.querydsl.jpa.impl.JPAQueryFactory;
import drive_only.drive_only_server.domain.SavedPlace;
import jakarta.persistence.EntityManager;
import java.util.List;

public class SavedPlaceRepositoryImpl implements SavedPlaceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SavedPlaceRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<SavedPlace> findSavedPlacesByMember(Long memberId, Long lastId, int sizePlusOne) {
        return queryFactory
                .selectFrom(savedPlace)
                .join(savedPlace.place, place).fetchJoin()
                .where(
                        savedPlace.member.id.eq(memberId),
                        lastId != null ? savedPlace.id.lt(lastId) : null
                )
                .orderBy(savedPlace.id.desc())
                .limit(sizePlusOne)
                .fetch();
    }
}