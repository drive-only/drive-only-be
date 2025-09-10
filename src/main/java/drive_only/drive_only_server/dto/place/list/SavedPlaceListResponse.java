package drive_only.drive_only_server.dto.place.list;

import drive_only.drive_only_server.dto.place.search.SavedPlaceSearchResponse;
import java.util.List;


public record SavedPlaceListResponse(
        List<SavedPlaceSearchResponse> data,
        Long lastId,
        int size,
        boolean hasNext
) {
    public static SavedPlaceListResponse from(List<SavedPlaceSearchResponse> data, Long lastId, int size, boolean hasNext) {
        return new SavedPlaceListResponse(data, lastId, size, hasNext);
    }
}
