package drive_only.drive_only_server.dto.course.list;

import drive_only.drive_only_server.dto.course.search.MyCourseSearchResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyCourseListResponse {
    private List<MyCourseSearchResponse> data;
    private Meta meta;

    public static MyCourseListResponse from(List<MyCourseSearchResponse> data, Long lastId, int size, boolean hasNext) {
        return MyCourseListResponse.builder()
                .data(data)
                .meta(Meta.builder()
                        .lastId(lastId)
                        .size(size)
                        .hasNext(hasNext)
                        .build())
                .build();
    }

    @Getter
    @Builder
    public static class Meta {
        private Long lastId;
        private int size;
        private boolean hasNext;
    }
}
