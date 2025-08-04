package drive_only.drive_only_server.service.client;

import drive_only.drive_only_server.dto.data.AreaCodeResponse;
import drive_only.drive_only_server.dto.data.DetailIntroResponse;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse;
import drive_only.drive_only_server.dto.place.nearbySearch.NearbyPlaceTourApiResponse;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
public class TourApiClient {
    private static final Retry RETRY = Retry.backoff(3, Duration.ofSeconds(2));
    private static final String MOBILE_OS = "ETC", MOBILE_APP = "DriveOnlyApp", TYPE = "json";
    private static final String PATH_AREA_CODE = "/ldongCode2", PATH_PLACE_LIST = "/areaBasedList2", PATH_PLACE_DETAIL = "/detailIntro2";
    private static final int AREA_PAGE = 1, AREA_ROWS = 1000;
    private static final int DEFAULT_RADIUS = 3000;
    private static final int DEFAULT_PAGE_NO = 1;

    private final WebClient webClient;
    @Value("${tourapi.service-key}")
    private String serviceKey;

    public List<AreaCodeResponse.Item> fetchAreaCodes(String regionCode) {
        AreaCodeResponse response = webClient.get()
                .uri(uriBuilder -> {
                    UriBuilder ub = uriBuilder.path(PATH_AREA_CODE)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("MobileOS", MOBILE_OS)
                            .queryParam("MobileApp", MOBILE_APP)
                            .queryParam("_type", TYPE)
                            .queryParam("pageNo", AREA_PAGE)
                            .queryParam("numOfRows", AREA_ROWS);
                    if (regionCode != null) {
                        ub = ub.queryParam("lDongRegnCd", regionCode);
                    }
                    return ub.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AreaCodeResponse.class)
                .retryWhen(RETRY)
                .block();

        if (!hasAreas(response)) {
            return null;
        }
        return response.response().body().items().item();
    }

    public List<PlaceDataInitResponse.Item> fetchPlaces(int pageNo, int numOfRows) {
        PlaceDataInitResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(PATH_PLACE_LIST)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", MOBILE_OS)
                        .queryParam("MobileApp", MOBILE_APP)
                        .queryParam("_type", TYPE)
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", numOfRows)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PlaceDataInitResponse.class)
                .retryWhen(RETRY)
                .block();

        if (!hasPlaces(response)) {
            return null;
        }

        return response.response().body().items().item().stream()
                .filter(place -> isValidContentTypeId(Integer.parseInt(place.contenttypeid())))
                .toList();
    }

    public DetailIntroResponse.Item fetchPlaceDetail(String contentId, String contentTypeId) {
        DetailIntroResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(PATH_PLACE_DETAIL)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", MOBILE_OS)
                        .queryParam("MobileApp", MOBILE_APP)
                        .queryParam("_type", TYPE)
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DetailIntroResponse.class)
                .retryWhen(RETRY)
                .block();

        if (!hasPlaceDetails(response)) {
            return null;
        }
        return response.response().body().items().item().get(0);
    }

    public List<NearbyPlaceTourApiResponse.Item> fetchNearbyPlaces(int contentTypeId, Double mapX, Double mapY, int numOfRows) {
        NearbyPlaceTourApiResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/locationBasedList2")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", MOBILE_OS)
                        .queryParam("MobileApp", MOBILE_APP)
                        .queryParam("_type", TYPE)
                        .queryParam("arrange", "E")
                        .queryParam("contentTypeId", contentTypeId)
                        .queryParam("mapX", mapX)
                        .queryParam("mapY", mapY)
                        .queryParam("radius", DEFAULT_RADIUS)
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("pageNo", DEFAULT_PAGE_NO)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(NearbyPlaceTourApiResponse.class)
                .block();

        if (!hasNearbyPlaces(response)) {
            return null;
        }
        return response.response().body().items().item();
    }

    private boolean hasAreas(AreaCodeResponse response) {
        return response != null &&
                response.response() != null &&
                response.response().body() != null &&
                response.response().body().items() != null &&
                response.response().body().items().item() != null;
    }

    private boolean hasPlaces(PlaceDataInitResponse response) {
        return response != null &&
                response.response() != null &&
                response.response().body() != null &&
                response.response().body().items() != null &&
                response.response().body().items().item() != null;
    }

    private boolean hasPlaceDetails(DetailIntroResponse response) {
        return response != null &&
                response.response() != null &&
                response.response().body() != null &&
                response.response().body().items() != null &&
                response.response().body().items().item() != null;
    }

    private boolean hasNearbyPlaces(NearbyPlaceTourApiResponse response) {
        return response != null &&
                response.response() != null &&
                response.response().body() != null &&
                response.response().body().items() != null &&
                response.response().body().items().item() != null;
    }

    private boolean isValidContentTypeId(int contentTypeId) {
        return contentTypeId == 12 || contentTypeId == 14 || contentTypeId == 38 || contentTypeId == 39;
    }
}
