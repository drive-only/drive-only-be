package drive_only.drive_only_server.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceDataInitResponse {
    private Response response;

    @Getter
    @Setter
    public static class Response {
        private Body body;
    }

    @Getter
    @Setter
    public static class Body {
        private Items items;
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
        private String addr1;
        private String addr2;
        private int contentid;
        private int contenttypeid;
        private int createdtime;
        private String firstimage2;
        private Double mapx;
        private Double mapy;
        private String tel;
        private String title;
    }
}
