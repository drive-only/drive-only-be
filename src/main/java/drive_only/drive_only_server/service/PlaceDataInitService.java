package drive_only.drive_only_server.service;

import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.DetailIntroResponse;
import drive_only.drive_only_server.dto.PlaceDataInitResponse;
import drive_only.drive_only_server.dto.PlaceDataInitResponse.Item;
import drive_only.drive_only_server.repository.PlaceRepository;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PlaceDataInitService {
    private final PlaceRepository placeRepository;
    private final WebClient webClient;

    public PlaceDataInitService(PlaceRepository placeRepository, WebClient webClient) {
        this.placeRepository = placeRepository;
        this.webClient = webClient;
    }

    public void importPlaceDataFromTourApi() {
        Mono<PlaceDataInitResponse> result = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/areaBasedList2")
                        .queryParam("serviceKey", "cQvLnSjhJGqaRDQw3oWGS3PLYZ%2F0mK2hywjRA07%2F1Gc455UdpgXjjyTwTQJxQcI52xi6nl%2By9XgDlhQEF5o9Uw%3D%3D")
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "운전만해")
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", 20)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PlaceDataInitResponse.class);

        result.subscribe(response -> {
            List<Item> items = response.getResponse()
                    .getBody()
                    .getItems()
                    .getItem();

            for (Item item : items) {
                int contentId = item.getContentid();

                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/detailIntro2")
                                .queryParam("serviceKey", "cQvLnSjhJGqaRDQw3oWGS3PLYZ%2F0mK2hywjRA07%2F1Gc455UdpgXjjyTwTQJxQcI52xi6nl%2By9XgDlhQEF5o9Uw%3D%3D")
                                .queryParam("MobileOS", "ETC")
                                .queryParam("MobileApp", "운전만해")
                                .queryParam("_type", "json")
                                .queryParam("contentId", contentId)
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(DetailIntroResponse.class)
                        .subscribe(detailIntroResponse -> {
                            if (item.getContenttypeid() == 12) {

                            }
                            new Place(item.getTitle(),
                                    item.getAddr1() + " " + item.getAddr2(),
                                    item.getFirstimage2(),
                                    )
                        })
            }
        });
    }
}
