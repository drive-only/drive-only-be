package drive_only.drive_only_server.service.client;

import drive_only.drive_only_server.dto.category.RegionResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TourApiClient {

    private final WebClient webClient;

    @Value("${tourapi.service-key}")
    private String serviceKey;

    private static final String BASE_PATH = "/areaCode2";

    public List<RegionResponse> fetchRegionData() {
        List<RegionResponse> result = new ArrayList<>();

        JSONArray regions = fetchAreaCodeItems(null); // 시/도 조회
        for (int i = 0; i < regions.length(); i++) {
            JSONObject regionObj = regions.getJSONObject(i);
            int areaCode = regionObj.getInt("code");
            String regionName = regionObj.getString("name");

            JSONArray subRegions = fetchAreaCodeItems(areaCode);
            List<String> subRegionList = new ArrayList<>();
            for (int j = 0; j < subRegions.length(); j++) {
                subRegionList.add(subRegions.getJSONObject(j).getString("name"));
            }

            result.add(new RegionResponse(regionName, subRegionList));
        }

        return result;
    }

    private JSONArray fetchAreaCodeItems(Integer areaCode) {
        try {
            StringBuilder uri = new StringBuilder(BASE_PATH)
                    .append("?ServiceKey=").append(serviceKey)
                    .append("&MobileOS=ETC")
                    .append("&MobileApp=DriveOnlyApp")
                    .append("&numOfRows=100")
                    .append("&_type=json");

            if (areaCode != null) {
                uri.append("&areaCode=").append(areaCode);
            }

            String response = webClient
                    .get()
                    .uri(uri.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();  // 블로킹 처리 (비동기 필요 시 변경 가능)

            System.out.println("API 응답 원문: " + response);

            JSONObject json = new JSONObject(response);
            return json.getJSONObject("response")
                    .getJSONObject("body")
                    .getJSONObject("items")
                    .getJSONArray("item");

        } catch (Exception e) {
            System.err.println("WebClient API 호출 실패: " + e.getMessage());
            return new JSONArray();
        }
    }
}
