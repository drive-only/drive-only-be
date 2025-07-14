package drive_only.drive_only_server.dto.place;

import drive_only.drive_only_server.dto.data.PlaceDataInitResponse;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse.Body;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse.Item;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse.Items;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse.Response;
import java.util.List;

public record NearbyPlaceTourApiResponse(
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
