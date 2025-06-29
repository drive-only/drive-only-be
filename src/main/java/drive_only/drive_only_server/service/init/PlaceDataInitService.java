package drive_only.drive_only_server.service.init;

import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.init.DetailIntroResponse;
import drive_only.drive_only_server.dto.init.PlaceDataInitResponse;
import drive_only.drive_only_server.dto.init.PlaceDataInitResponse.Item;
import drive_only.drive_only_server.repository.PlaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlaceDataInitService {
    @Value("${tourapi.service-key}")
    private String tourApiServiceKey;
    private final PlaceRepository placeRepository;
    private final WebClient webClient;

    public void importPlaceDataFromTourApi() {
        int numOfRows = 10;
        int totalPage = 5;
        //TODO : 현재는 개발 계정으로 TourAPI와 연동하고 있어서, 나중에 운영 계정으로 변환되면 전체 데이터를 가져오도록 아래처럼 변경
        //int numOfRows = 200;
        //int totalCount = calculateTotalCount();
        //int totalPage = (int) Math.ceil((double) totalCount / numOfRows);

        for (int pageNo = 1; pageNo <= totalPage; pageNo++) {
            List<Item> places = getAllPlaces(pageNo, numOfRows);
            if (places == null) {
                continue;
            }
            savePlaces(places);
        }
    }

    private int calculateTotalCount() {
        PlaceDataInitResponse response = requestPlaceDataFromTourApi(1, 1).block();
        return response.getResponse().getBody().getTotalCount();
    }

    private List<Item> getAllPlaces(int pageNo, int numOfRows) {
        PlaceDataInitResponse response = requestPlaceDataFromTourApi(pageNo, numOfRows).block();

        if (isValidResponse(response)) {
            return null;
        }
        return response.getResponse().getBody().getItems().getItem();
    }

    private Mono<PlaceDataInitResponse> requestPlaceDataFromTourApi(int pageNo, int numOfRows) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/areaBasedList2")
                        .queryParam("serviceKey", tourApiServiceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "justdrive")
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("pageNo", pageNo)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PlaceDataInitResponse.class);
    }

    private boolean isValidResponse(PlaceDataInitResponse response) {
        boolean valid = response != null &&
                response.getResponse() != null &&
                response.getResponse().getBody() != null &&
                response.getResponse().getBody().getItems() != null &&
                response.getResponse().getBody().getItems().getItem() != null;
        return !valid;
    }

    private void savePlaces(List<Item> places) {
        for (Item place : places) {
            String contentId = place.getContentid();
            String contentTypeId = place.getContenttypeid();

            DetailIntroResponse detailIntroResponse = requestPlaceDetailFromTourApi(place, contentId, contentTypeId).block();
            if (detailIntroResponse == null) {
                continue;
            }

            List<DetailIntroResponse.Item> detailItems = detailIntroResponse.getResponse().getBody().getItems().getItem();
            if (detailItems == null || detailItems.isEmpty()) {
                continue;
            }

            DetailIntroResponse.Item placeDetail = detailItems.get(0);
            Place newPlace = createPlace(place, placeDetail);
            placeRepository.save(newPlace);
        }
    }

    private Mono<DetailIntroResponse> requestPlaceDetailFromTourApi(Item place, String contentId, String contentTypeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/detailIntro2")
                        .queryParam("serviceKey", tourApiServiceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "justdrive")
                        .queryParam("_type", "json")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DetailIntroResponse.class);
    }

    private Place createPlace(Item place, DetailIntroResponse.Item placeDetail) {
        int contentTypeId = Integer.parseInt(place.getContenttypeid());
        String useTime = getUseTime(contentTypeId, placeDetail);
        String restDate = getRestDate(contentTypeId, placeDetail);

        return new Place(
                place.getTitle(),
                place.getAddr1(),
                place.getFirstimage2(),
                useTime,
                restDate,
                place.getTel(),
                Double.parseDouble(place.getMapx()),
                Double.parseDouble(place.getMapy())
        );
    }

    private String getUseTime(int contentTypeId, DetailIntroResponse.Item placeDetail) {
        return switch (contentTypeId) {
            case 12 -> placeDetail.getUsetime();
            case 14 -> placeDetail.getUsetimeculture();
            case 15 -> placeDetail.getPlaytime();
            case 28 -> placeDetail.getUsetimeleports();
            case 38 -> placeDetail.getOpentime();
            default -> placeDetail.getOpentimefood();
        };
    }

    private String getRestDate(int contentTypeId, DetailIntroResponse.Item placeDetail) {
        return switch (contentTypeId) {
            case 12 -> placeDetail.getRestdate();
            case 14 -> placeDetail.getRestdateculture();
            case 15 -> "";
            case 28 -> placeDetail.getRestdateleports();
            case 38 -> placeDetail.getRestdateshopping();
            default -> placeDetail.getRestdatefood();
        };
    }
}
