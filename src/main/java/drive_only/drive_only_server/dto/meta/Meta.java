package drive_only.drive_only_server.dto.meta;

import drive_only.drive_only_server.domain.Comment;
import org.springframework.data.domain.Page;

public record Meta(
        int total,
        int page,
        int size,
        Boolean hasNext
) {
    public static Meta from(Page<Comment> parentComments) {
        return new Meta(
                (int) parentComments.getTotalElements(),
                parentComments.getNumber() + 1,
                parentComments.getSize(),
                parentComments.hasNext()
        );
    }
}
