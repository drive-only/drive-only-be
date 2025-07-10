package drive_only.drive_only_server.dto.comment.search;

import drive_only.drive_only_server.dto.meta.Meta;
import java.util.List;

public record CommentSearchListResponse(
        List<CommentSearchResponse> data,
        Meta meta
) {}
