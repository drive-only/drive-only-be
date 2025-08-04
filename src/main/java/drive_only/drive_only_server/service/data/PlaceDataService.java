package drive_only.drive_only_server.service.data;

import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.data.AreaCodeResponse;
import drive_only.drive_only_server.dto.data.DetailIntroResponse;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse.Item;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PlaceDataService {
    @Value("${tourapi.service-key}")
    private String tourApiServiceKey;
    private final PlaceRepository placeRepository;
    private final WebClient webClient;

    private final Map<String, String> regionCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> subRegionCache = new ConcurrentHashMap<>();

    //TODO : 현재는 개발 계정으로 TourAPI와 연동하고 있어서, 나중에 운영 계정으로 변환되면 전체 데이터를 가져오도록 아래처럼 변경
    //int numOfRows = 200;
    //int totalCount = calculateTotalCount();
    //int totalPage = (int) Math.ceil((double) totalCount / numOfRows);
    private final static int DEFAULT_ROWS = 100;
    private static final int AREA_CODE_START_PAGE = 1;
    private final static int PLACE_START_PAGE = 1;
    private final static int PLACE_TOTAL_PAGES = 7;
    private static final int API_MAX_ROWS = 1000;
    private static final Retry RETRY_POLICY = Retry.backoff(3, Duration.ofSeconds(2));

    private static final String  PATH_AREA_CODE    = "/ldongCode2";
    private static final String  PATH_PLACE_LIST   = "/areaBasedList2";
    private static final String  PATH_PLACE_DETAIL = "/detailIntro2";

    @PostConstruct
    public void initRegionCache() {
        AreaCodeResponse allRegionResponse = fetchAreaCodes(null);
        if (isAreaCodeResponseInvalid(allRegionResponse)) {
            return;
        }
        cacheRegions(allRegionResponse);
        regionCache.keySet().forEach(this::cacheSubRegions);
    }

    @Transactional
    public void importAllPlaces() {
        for (int pageNo = PLACE_START_PAGE; pageNo <= PLACE_TOTAL_PAGES; pageNo++) {
            List<Item> places = fetchPlacesPage(pageNo);
            if (places == null) {
                continue;
            }
            savePlaces(places);
        }
    }

    @Transactional
    @Scheduled(cron = "0 50 4 * * *")
    public void syncAllPlaces() {
        for (int pageNo = PLACE_START_PAGE; pageNo <= PLACE_TOTAL_PAGES; pageNo++) {
            List<Item> places = fetchPlacesPage(pageNo);
            if (places == null) {
                continue;
            }
            for (Item place : places) {
                syncPlace(place);
            }
        }
    }

    private AreaCodeResponse fetchAreaCodes(String regionCode) {
        return webClient.get()
                .uri(uriBuilder -> {
                    UriBuilder ub = addCommonQueryParams(uriBuilder, PATH_AREA_CODE)
                            .queryParam("pageNo", AREA_CODE_START_PAGE)
                            .queryParam("numOfRows", API_MAX_ROWS);
                    if (regionCode != null) {
                        ub = ub.queryParam("lDongRegnCd", regionCode);
                    }
                    return ub.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AreaCodeResponse.class)
                .retryWhen(RETRY_POLICY)
                .block();
    }

    private boolean isAreaCodeResponseInvalid(AreaCodeResponse response) {
        return response == null
                || response.response() == null
                || response.response().body() == null
                || response.response().body().items() == null;
    }

    private void cacheRegions(AreaCodeResponse response) {
        for (AreaCodeResponse.Item item : response.response().body().items().item()) {
            regionCache.put(item.code().trim(), item.name().trim());
        }
    }

    private void cacheSubRegions(String regionCode) {
        AreaCodeResponse subResponse = fetchAreaCodes(regionCode);
        if (isAreaCodeResponseInvalid(subResponse)) {
            return;
        }

        Map<String, String> subRegionsMap = new ConcurrentHashMap<>();
        for (AreaCodeResponse.Item item : subResponse.response().body().items().item()) {
            subRegionsMap.put(item.code().trim(), item.name().trim());
        }
        subRegionCache.put(regionCode, subRegionsMap);
    }

    private List<Item> fetchPlacesPage(int pageNo) {
        PlaceDataInitResponse response = requestPlaceResponse(pageNo).block();
        if (isPlaceResponseInvalid(response)) {
            return null;
        }
        return response.response().body().items().item();
    }

    private Mono<PlaceDataInitResponse> requestPlaceResponse(int pageNo) {
        return webClient.get()
                .uri(uriBuilder -> {
                    UriBuilder ub = addCommonQueryParams(uriBuilder, PATH_PLACE_LIST);
                    return ub
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", DEFAULT_ROWS)
                            .build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PlaceDataInitResponse.class)
                .retryWhen(RETRY_POLICY);
    }

    private boolean isPlaceResponseInvalid(PlaceDataInitResponse response) {
        boolean valid = response != null &&
                response.response() != null &&
                response.response().body() != null &&
                response.response().body().items() != null &&
                response.response().body().items().item() != null;
        return !valid;
    }

    private void savePlaces(List<Item> places) {
        for (Item place : places) {
            String contentId = place.contentid();
            String contentTypeId = place.contenttypeid();

            DetailIntroResponse detailIntroResponse = fetchPlaceDetailResponse(contentId, contentTypeId).block();
            if (detailIntroResponse == null) {
                continue;
            }

            List<DetailIntroResponse.Item> detailItems = detailIntroResponse.response().body().items().item();
            if (detailItems == null || detailItems.isEmpty()) {
                continue;
            }

            DetailIntroResponse.Item placeDetail = detailItems.get(0);
            Place newPlace = createPlace(place, placeDetail);
            placeRepository.save(newPlace);
        }
    }

    private Mono<DetailIntroResponse> fetchPlaceDetailResponse(String contentId, String contentTypeId) {
        return webClient.get()
                .uri(uriBuilder -> {
                    UriBuilder ub = addCommonQueryParams(uriBuilder, PATH_PLACE_DETAIL);
                    return ub
                            .queryParam("contentId", contentId)
                            .queryParam("contentTypeId", contentTypeId)
                            .build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DetailIntroResponse.class)
                .retryWhen(RETRY_POLICY);
    }

    private void syncPlace(Item place) {
        DetailIntroResponse detail = fetchPlaceDetailResponse(place.contentid(), place.contenttypeid()).block();
        if (detail == null) {
            return;
        }

        Optional<Place> findPlace = placeRepository.findByContentId(Integer.parseInt(place.contentid()));
        if (findPlace.isPresent()) {
            Place existingPlace = findPlace.get();
            updateExistingPlace(existingPlace, place, detail);
        } else {
            Place newPlace = createPlace(place, detail.response().body().items().item().get(0));
            placeRepository.save(newPlace);
        }
    }

    private Place createPlace(Item place, DetailIntroResponse.Item placeDetail) {
        int contentId = Integer.parseInt(place.contentid());
        int contentTypeId = Integer.parseInt(place.contenttypeid());
        String region = getRegion(place.lDongRegnCd());
        String subRegion = getSubRegion(place.lDongRegnCd(), place.lDongSignguCd());
        String useTime = selectUseTime(contentTypeId, placeDetail);
        String restDate = selectRestDate(contentTypeId, placeDetail);

        return new Place(
                contentId,
                contentTypeId,
                place.title(),
                place.addr1() + " " + place.addr2(),
                region,
                subRegion,
                place.firstimage2(),
                useTime,
                restDate,
                place.tel(),
                Double.parseDouble(place.mapx()),
                Double.parseDouble(place.mapy())
        );
    }

    private void updateExistingPlace(Place existingPlace, Item place, DetailIntroResponse detail) {
        existingPlace.updateBasicInfo(
                place.title(),
                place.addr1() + " " + place.addr2(),
                getRegion(place.lDongRegnCd()),
                getSubRegion(place.lDongRegnCd(), place.lDongSignguCd()),
                place.firstimage2(),
                place.tel(),
                Double.parseDouble(place.mapx()),
                Double.parseDouble(place.mapy())
        );

        int contentTypeId = Integer.parseInt(place.contenttypeid());
        DetailIntroResponse.Item placeDetail = detail.response().body().items().item().get(0);
        existingPlace.updateDetailInfo(
                selectUseTime(contentTypeId, placeDetail),
                selectRestDate(contentTypeId, placeDetail)
        );
    }

    private UriBuilder addCommonQueryParams(UriBuilder ub, String path) {
        return ub
                .path(path)
                .queryParam("serviceKey", tourApiServiceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "drive-only")
                .queryParam("_type", "json");
    }

    private String getRegion(String lDongRegnCd) {
        return regionCache.get(lDongRegnCd);
    }

    private String getSubRegion(String lDongRegnCd, String lDongSignguCd) {
        Map<String, String> subMap = subRegionCache.get(lDongRegnCd);
        return (subMap != null) ? subMap.get(lDongSignguCd) : null;
    }

    private String selectUseTime(int typeId, DetailIntroResponse.Item item) {
        return switch (typeId) {
            case 12 -> item.usetime();
            case 14 -> item.usetimeculture();
            case 15 -> item.playtime();
            case 28 -> item.usetimeleports();
            case 38 -> item.opentime();
            case 39 -> item.opentimefood();
            default -> "";
        };
    }

    private String selectRestDate(int typeId, DetailIntroResponse.Item item) {
        return switch (typeId) {
            case 12 -> item.restdate();
            case 14 -> item.restdateculture();
            case 28 -> item.restdateleports();
            case 38 -> item.restdateshopping();
            case 39 -> item.restdatefood();
            default -> "";
        };
    }
}
