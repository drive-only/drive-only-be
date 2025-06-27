package drive_only.drive_only_server.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailIntroResponse {
    private Response response;

    @Getter
    @Setter
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @Setter
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @Setter
    public static class Body {
        private Items items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Getter
    @Setter
    public static class Items {
        private List<Item> item;
    }

    @Getter
    @Setter
    public static class Item {
        //공통정보
        private String contentid;
        private String contenttypeid;

        //관광지(contentTypeId = 12)
        private String usetime;
        private String restdate;

        //문화시설(contentTypeId = 14)
        private String usetimeculture;
        private String restdateculture;

        //행사/공연/축제(contentTypeId = 15)
        private String playtime;

        //레포츠(contentTypeId = 28)
        private String usetimeleports;
        private String restdateleports;

        //쇼핑(contentTypeId = 38)
        private String opentime;
        private String restdateshopping;

        //음식점(contentTypeId = 39)
        private String opentimefood;
        private String restdatefood;
    }
}
