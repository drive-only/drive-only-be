package drive_only.drive_only_server.service;

import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.DetailIntroResponse;
import drive_only.drive_only_server.dto.PlaceDataInitResponse;
import drive_only.drive_only_server.dto.PlaceDataInitResponse.Item;
import drive_only.drive_only_server.repository.PlaceRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PlaceDataInitService {
    @Value("${tourapi.service-key}")
    private String tourApiServiceKey;
    private final PlaceRepository placeRepository;
    private final WebClient webClient;

    public PlaceDataInitService(PlaceRepository placeRepository, WebClient webClient) {
        this.placeRepository = placeRepository;
        this.webClient = webClient;
    }

    public void importPlaceDataFromTourApi() {
        int numOfRows = 1000;
        int totalCount = calculateTotalCount();
        int totalPage = (int) Math.ceil((double) totalCount / numOfRows);

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
                        .queryParam("MobileApp", "운전만해")
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("pageNo", pageNo)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PlaceDataInitResponse.class);
    }

    private boolean isValidResponse(PlaceDataInitResponse response) {
        return response != null &&
                response.getResponse() != null &&
                response.getResponse().getBody() != null &&
                response.getResponse().getBody().getItems() != null &&
                response.getResponse().getBody().getItems().getItem() != null;
    }

    private void savePlaces(List<Item> places) {
        for (Item place : places) {
            int contentId = place.getContentid();
            int contentTypeId = place.getContenttypeid();

            requestPlaceDetailFromTourApi(place, contentId, contentTypeId)
                    .subscribe(detailIntroResponse -> {
                        DetailIntroResponse.Item placeDetail = detailIntroResponse.getResponse().getBody().getItems().getItem().get(0);
                        Place newPlace = createPlace(place, placeDetail);
                        placeRepository.save(newPlace);
                    });
        }
    }

    private Mono<DetailIntroResponse> requestPlaceDetailFromTourApi(Item place, int contentId, int contentTypeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/detailIntro2")
                        .queryParam("serviceKey", tourApiServiceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "운전만해")
                        .queryParam("_type", "json")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DetailIntroResponse.class);
    }

    private static Place createPlace(Item place, DetailIntroResponse.Item placeDetail) {
        String useTime = "";
        String restDate = "";
        int contentTypeId = place.getContenttypeid();

        if (contentTypeId == 12) {
            useTime = placeDetail.getUsetime();
            restDate = placeDetail.getRestdate();
        }
        return new Place(place.getTitle(),
                place.getAddr1() + " " + place.getAddr2(),
                place.getFirstimage2(),
                useTime,
                restDate,
                place.getTel(),
                place.getMapx(),
                place.getMapy()
        );
    }
}
