package drive_only.drive_only_server.dto.data.tourapi;

import java.util.List;

public record DetailIntroResponse(
        Response response
) {
    public record Response(
            Header header,
            Body body
    ) {
    }

    public record Header(
            String resultCode,
            String resultMsg
    ) {
    }

    public record Body(
            Items items,
            int numOfRows,
            int pageNo,
            int totalCount
    ) {
    }

    public record Items(
            List<Item> item
    ) {
    }

    public record Item(
            String contentid,
            String contenttypeid,

            // 관광지(contentTypeId = 12)
            String usetime,
            String restdate,

            // 문화시설(contentTypeId = 14)
            String usetimeculture,
            String restdateculture,

            // 쇼핑(contentTypeId = 38)
            String opentime,
            String restdateshopping,

            // 음식점(contentTypeId = 39)
            String opentimefood,
            String restdatefood
    ) {
    }
}
