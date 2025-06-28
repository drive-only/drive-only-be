package drive_only.drive_only_server.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaceDataInitResponse {
    private Response response;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {
        private Body body;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Body {
        private Items items;
        private int totalCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Items {
        private List<Item> item;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Item {
        private String addr1;
        private String addr2;
        private String contentid;
        private String contenttypeid;
        private String createdtime;
        private String firstimage2;
        private String mapx;
        private String mapy;
        private String tel;
        private String title;
    }
}
