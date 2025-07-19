package drive_only.drive_only_server.dto.meta;

import org.springframework.data.domain.Page;

public record Meta(
        int total,
        int page,
        int size,
        Boolean hasNext
) {
    public static <T> Meta from(Page<T> page) {
        return new Meta(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.hasNext()
        );
    }
}
