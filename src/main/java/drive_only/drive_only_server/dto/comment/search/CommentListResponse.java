package drive_only.drive_only_server.dto.comment.search;

import java.util.List;

public record CommentListResponse(
        List<CommentSearchResponse> data,
        Meta meta
) {
    public record Meta(
            int total,
            int page,
            int size,
            Boolean hasNext
    ) {}
}
