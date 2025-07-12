package drive_only.drive_only_server.dto.data;

import java.util.List;

public record AreaCodeResponse(
        Response response
) {
    public record Response(
            Body body
    ) {}

    public record Body(
            Items items,
            int totalCount
    ) {}

    public record Items(
            List<Item> item
    ) {}

    public record Item(
            String code,
            String name
    ) {}
}
