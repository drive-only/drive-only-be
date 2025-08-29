package drive_only.drive_only_server.repository.course;

import drive_only.drive_only_server.domain.SavedPlace;
import java.util.List;

public interface SavedPlaceRepositoryCustom {
    List<SavedPlace> findSavedPlacesByMember(Long memberId, Long lastId, int sizePlusOne);
}