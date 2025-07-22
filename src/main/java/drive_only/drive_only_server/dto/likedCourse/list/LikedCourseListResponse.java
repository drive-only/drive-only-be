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

    public static LikedCourseListResponse from(List<LikedCourseSearchResponse> data, Long lastId, int size, boolean hasNext) {
        return LikedCourseListResponse.builder()
                .data(data)
                .meta(Meta.builder()
                        .lastId(lastId)
                        .size(size)
                        .hasNext(hasNext)
                        .build())
                .build();
    }

    @Builder
    @Getter
    public static class Meta {
        private Long lastId;
        private int size;
        private boolean hasNext;
    }
}