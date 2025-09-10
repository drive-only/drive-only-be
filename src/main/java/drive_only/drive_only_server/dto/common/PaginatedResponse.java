package drive_only.drive_only_server.dto.common;

import drive_only.drive_only_server.dto.meta.Meta;
import jakarta.annotation.Nullable;
import java.util.List;

public record PaginatedResponse<T>(
        List<T> data,
        @Nullable Meta meta
) {
}
