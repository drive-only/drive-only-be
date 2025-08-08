package drive_only.drive_only_server.dto.place.nearbySearch;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record NearbyPlaceTourApiResponse(
    Response response
) {
    public record Response(Body body) {}

    public record Body(
            @JsonDeserialize(using = LenientItemsDeserializer.class)
            List<Item> items,
            int totalCount
    ) {}

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
    ) {}

    public static class LenientItemsDeserializer extends JsonDeserializer<List<Item>> {
        @Override
        public List<Item> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = p.getCodec();
            JsonNode node = codec.readTree(p);

            if (node == null || node.isNull() || (node.isTextual() && node.asText().isEmpty())) {
                return Collections.emptyList();
            }

            if (node.isObject()) {
                JsonNode itemNode = node.get("item");
                if (itemNode == null || itemNode.isNull()) return Collections.emptyList();

                ObjectMapper om = (ObjectMapper) codec;

                if (itemNode.isArray()) {
                    Item[] arr = om.treeToValue(itemNode, Item[].class);
                    return arr == null ? Collections.emptyList() : Arrays.asList(arr);
                }
                Item single = om.treeToValue(itemNode, Item.class);
                return single == null ? Collections.emptyList() : List.of(single);
            }

            return Collections.emptyList();
        }
    }
}
