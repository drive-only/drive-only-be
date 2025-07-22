package drive_only.drive_only_server.dto.likedCourse.list;

import drive_only.drive_only_server.dto.likedCourse.search.LikedCourseSearchResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LikedCourseListResponse {
    private List<LikedCourseSearchResponse> data;
    private Meta meta;

    @Builder
    @Getter
    public static class Meta {
        private Long lastId;
        private int size;
        private boolean hasNext;
    }
}
