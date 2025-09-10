package drive_only.drive_only_server.dto.data.tourapi;

import java.util.List;

public record PlaceDataInitResponse(
        Response response
) {
    public record Response(
            Body body
    ) {
    }

    public record Body(
            Items items,
            int totalCount
    ) {
    }

    public record Items(
            List<Item> item
    ) {
    }

    public record Item(
            String addr1,
            String addr2,
            String lDongRegnCd,
            String lDongSignguCd,
            String contentid,
            String contenttypeid,
            String createdtime,
            String firstimage2,
            String mapx,
            String mapy,
            String tel,
            String title
    ) {
    }
}
