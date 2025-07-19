package drive_only.drive_only_server.service.data;

import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.data.AreaCodeResponse;
import drive_only.drive_only_server.dto.data.DetailIntroResponse;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse.Item;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlaceDataService {
    @Value("${tourapi.service-key}")
    private String tourApiServiceKey;
    private final PlaceRepository placeRepository;
    private final WebClient webClient;

    private final static int numOfRows = 10;
    private final static int startPage = 1;
    private final static int totalPage = 10;
    //TODO : 현재는 개발 계정으로 TourAPI와 연동하고 있어서, 나중에 운영 계정으로 변환되면 전체 데이터를 가져오도록 위의 코드를 아래처럼 변경
    //int numOfRows = 200;
    //int totalCount = calculateTotalCount();
    //int totalPage = (int) Math.ceil((double) totalCount / numOfRows);

    public void importPlaceDataFromTourApi() {
        for (int pageNo = startPage; pageNo <= totalPage; pageNo++) {
            List<Item> places = getAllPlaces(pageNo, numOfRows);
            if (places == null) {
                continue;
            }
            savePlaces(places);
        }
    }

    @Scheduled(cron = "0 50 4 * * *")
    public void syncPlaceDataFromTourApi() {
        log.info("관광지 동기화 시작: {}", LocalDateTime.now());
        for (int pageNo = 1; pageNo <= totalPage; pageNo++) {
            List<Item> places = getAllPlaces(pageNo, numOfRows);
            if (places == null) {
                continue;
            }
            for (Item place : places) {
                syncPlaces(place);
            }
        }
        log.info("관광지 동기화 종료: {}", LocalDateTime.now());
    }

    private int calculateTotalCount() {
        PlaceDataInitResponse response = requestPlaceDataFromTourApi(1, 1).block();
        return response.response().body().totalCount();
    }

    private List<Item> getAllPlaces(int pageNo, int numOfRows) {
        PlaceDataInitResponse response = requestPlaceDataFromTourApi(pageNo, numOfRows).block();
        if (isInvalidResponse(response)) {
            return null;
        }
        return response.response().body().items().item();
    }

    private Mono<PlaceDataInitResponse> requestPlaceDataFromTourApi(int pageNo, int numOfRows) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/areaBasedList2")
                        .queryParam("serviceKey", tourApiServiceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "drive-only")
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("pageNo", pageNo)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PlaceDataInitResponse.class);
    }

    private boolean isInvalidResponse(PlaceDataInitResponse response) {
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

            DetailIntroResponse detailIntroResponse = requestPlaceDetailFromTourApi(contentId, contentTypeId).block();
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

    private Mono<DetailIntroResponse> requestPlaceDetailFromTourApi(String contentId, String contentTypeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/detailIntro2")
                        .queryParam("serviceKey", tourApiServiceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "drive-only")
                        .queryParam("_type", "json")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DetailIntroResponse.class);
    }

    private Place createPlace(Item place, DetailIntroResponse.Item placeDetail) {
        int contentId = Integer.parseInt(place.contentid());
        int contentTypeId = Integer.parseInt(place.contenttypeid());
        String region = getRegion(place.lDongRegnCd());
        String subRegion = getSubRegion(place.lDongRegnCd(), place.lDongSignguCd());
        String useTime = getUseTime(contentTypeId, placeDetail);
        String restDate = getRestDate(contentTypeId, placeDetail);

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

    private String getRegion(String lDongRegnCd) {
        AreaCodeResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ldongCode2")
                        .queryParam("serviceKey", tourApiServiceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "drive-only")
                        .queryParam("_type", "json")
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 20)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AreaCodeResponse.class)
                .block();

        return response.response().body().items().item().stream()
                .filter(item -> item.code().trim().equals(lDongRegnCd.trim()))
                .map(AreaCodeResponse.Item::name)
                .findFirst()
                .orElse(null);
    }

    private String getSubRegion(String lDongRegnCd, String lDongSignguCd) {
        AreaCodeResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ldongCode2")
                        .queryParam("serviceKey", tourApiServiceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "drive-only")
                        .queryParam("lDongRegnCd", lDongRegnCd)
                        .queryParam("_type", "json")
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 70)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AreaCodeResponse.class)
                .block();

        return response.response().body().items().item().stream()
                .filter(item -> item.code().trim().equals(lDongSignguCd.trim()))
                .map(AreaCodeResponse.Item::name)
                .findFirst()
                .orElse(null);
    }

    private String getUseTime(int contentTypeId, DetailIntroResponse.Item placeDetail) {
        return switch (contentTypeId) {
            case 12 -> placeDetail.usetime();
            case 14 -> placeDetail.usetimeculture();
            case 15 -> placeDetail.playtime();
            case 28 -> placeDetail.usetimeleports();
            case 32 -> "";
            case 38 -> placeDetail.opentime();
            case 30 -> "";
            default -> placeDetail.opentimefood();
        };
    }

    private String getRestDate(int contentTypeId, DetailIntroResponse.Item placeDetail) {
        return switch (contentTypeId) {
            case 12 -> placeDetail.restdate();
            case 14 -> placeDetail.restdateculture();
            case 15 -> "";
            case 28 -> placeDetail.restdateleports();
            case 32 -> "";
            case 38 -> placeDetail.restdateshopping();
            case 39 -> "";
            default -> placeDetail.restdatefood();
        };
    }

    private void syncPlaces(Item place) {
        DetailIntroResponse detail = requestPlaceDetailFromTourApi(place.contentid(), place.contenttypeid()).block();
        if (detail == null) {
            return;
        }

        Optional<Place> findPlace = placeRepository.findByContentId(Integer.parseInt(place.contentid()));
        if (findPlace.isPresent()) {
            Place existingPlace = findPlace.get();
            updatePlace(existingPlace, place, detail);
        } else {
            Place newPlace = createPlace(place, detail.response().body().items().item().get(0));
            placeRepository.save(newPlace);
        }
    }

    private void updatePlace(Place existingPlace, Item place, DetailIntroResponse detail) {
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
                getUseTime(contentTypeId, placeDetail),
                getRestDate(contentTypeId, placeDetail)
        );
    }
}
