package drive_only.drive_only_server.dto.meta;

public record Meta(
        int total,
        int page,
        int size,
        Boolean hasNext
) {
}
