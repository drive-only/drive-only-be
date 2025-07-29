package drive_only.drive_only_server.service.place;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.domain.SavedPlace;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.meta.Meta;
import drive_only.drive_only_server.dto.place.myPlace.DeleteSavedPlaceResponse;
import drive_only.drive_only_server.dto.place.myPlace.SavePlaceResponse;
import drive_only.drive_only_server.dto.place.nearbySearch.NearbyPlaceTourApiResponse;
import drive_only.drive_only_server.dto.place.nearbySearch.NearbyPlaceTourApiResponse.Item;
import drive_only.drive_only_server.dto.place.nearbySearch.NearbyPlacesResponse;
import drive_only.drive_only_server.dto.place.search.PlaceSearchRequest;
import drive_only.drive_only_server.dto.place.search.PlaceSearchResponse;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.custom.CourseNotFoundException;
import drive_only.drive_only_server.exception.custom.PlaceNotFoundException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.repository.course.SavedPlaceRepository;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import drive_only.drive_only_server.security.LoginMemberProvider;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceService {
    private static final int DEFAULT_RADIUS = 3000;
    private static final int DEFAULT_PAGE_NO = 1;
    private static final String SUCCESS_CREATE = "해당 장소가 성공적으로 저장되었습니다.";
    private static final String SUCCESS_DELETE = "저장된 장소를 성공적으로 삭제했습니다.";

    @Value("${tourapi.service-key}")
    private String tourApiServiceKey;
    private final WebClient webClient;
    private final PlaceRepository placeRepository;
    private final CourseRepository courseRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final LoginMemberProvider loginMemberProvider;

    public PaginatedResponse<PlaceSearchResponse> searchPlaces(PlaceSearchRequest request, int page, int size) {
        validateSearchRequest(request);
        Page<Place> places = placeRepository.searchPlaces(request, PageRequest.of(page, size));
        List<PlaceSearchResponse> responses = places.stream()
                .map(PlaceSearchResponse::from)
                .toList();
        Meta meta = Meta.from(places);
        return new PaginatedResponse<>(responses, meta);
    }

    public PaginatedResponse<NearbyPlacesResponse> searchNearbyPlaces(Long courseId, String type) {
        Course course = findCourse(courseId);
        List<CoursePlace> coursePlaces = course.getCoursePlaces();
        int coursePlaceCount = coursePlaces.size();
        List<Integer> distribution = getDistribution(coursePlaceCount);
        List<NearbyPlacesResponse> results = new ArrayList<>();

        for (int i = 0; i < coursePlaceCount; i++) {
            results.add(createNearbyPlacesResponse(coursePlaces.get(i), getContentTypeId(type), distribution.get(i)));
        }

        return new PaginatedResponse<>(results, null);
    }

    public PaginatedResponse<PlaceSearchResponse> searchSavedPlaces() {
        Member member = loginMemberProvider.getLoginMember();
        List<SavedPlace> savedPlaces = savedPlaceRepository.findByMember(member);
        List<PlaceSearchResponse> results = savedPlaces.stream()
                .map(savedPlace -> {
                    Place place = savedPlace.getPlace();
                    return PlaceSearchResponse.from(place);
                })
                .toList();
        return new PaginatedResponse<>(results, null);
    }

    @Transactional
    public SavePlaceResponse savePlace(Long placeId) {
        Member member = loginMemberProvider.getLoginMember();
        Place place = findPlaceById(placeId);
        SavedPlace savedPlace = savedPlaceRepository.save(new SavedPlace(member, place));
        return new SavePlaceResponse(savedPlace.getId(), SUCCESS_CREATE);
    }

    @Transactional
    public DeleteSavedPlaceResponse deleteSavedPlace(Long savedPlaceId) {
        Member member = loginMemberProvider.getLoginMember();
        SavedPlace savedPlace = member.getSavedPlaces().stream()
                .filter(sp -> sp.getId().equals(savedPlaceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("저장되지 않은 장소입니다."));
        savedPlaceRepository.delete(savedPlace);
        return new DeleteSavedPlaceResponse(savedPlace.getId(), SUCCESS_DELETE);
    }

    private List<Integer> getDistribution(int coursePlaceCount) {
        return switch (coursePlaceCount) {
            case 1 -> List.of(5);
            case 2 -> List.of(3, 2);
            case 3 -> List.of(2, 2, 1);
            case 4 -> List.of(2, 1, 1, 1);
            case 5 -> List.of(1, 1, 1, 1, 1);
            default -> throw new IllegalArgumentException("코스는 1개 이상 5개 이하이어야 합니다. 코스 개수: " + coursePlaceCount);
        };
    }

    private int getContentTypeId(String type) {
        return switch (type) {
            case "tourist-spot" -> 12;
            case "restaurant" -> 39;
            default -> throw new IllegalArgumentException("지원하지 않는 장소 타입입니다.");
        };
    }

    private NearbyPlacesResponse createNearbyPlacesResponse(CoursePlace coursePlace, int contentTypeId, int numOfRows) {
        Double mapX = coursePlace.getPlace().getLat();
        Double mapY = coursePlace.getPlace().getLng();

        List<Item> nearbyItems = getNearbyPlaces(contentTypeId, mapX, mapY, numOfRows)
                .response().body().items().item();
        List<PlaceSearchResponse> searchResponses = createPlaceSearchResponses(nearbyItems);

        return NearbyPlacesResponse.from(coursePlace, searchResponses);
    }

    private NearbyPlaceTourApiResponse getNearbyPlaces(int contentTypeId, Double mapX, Double mapY, int numOfRows) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/locationBasedList2")
                        .queryParam("serviceKey", tourApiServiceKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "drive-only")
                        .queryParam("_type", "json")
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
    }

    private List<PlaceSearchResponse> createPlaceSearchResponses(List<Item> nearbyPlaces) {
        return nearbyPlaces.stream()
                .map(nearbyPlace -> {
                    Place place = findPlaceByContentId(nearbyPlace);
                    return PlaceSearchResponse.from(place);
                })
                .toList();
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId).orElseThrow(CourseNotFoundException::new);
    }

    private Place findPlaceByContentId(Item place) {
        return placeRepository.findByContentId(Integer.parseInt(place.contentid())).orElseThrow(PlaceNotFoundException::new);
    }

    private Place findPlaceById(Long placeId) {
        return placeRepository.findById(placeId).orElseThrow(PlaceNotFoundException::new);
    }

    private void validateSearchRequest(PlaceSearchRequest request) {
        if (request.region() == null && request.subRegion() == null && request.keyword() == null) {
            throw new BusinessException(ErrorCode.KEYWORD_REQUIRED);
        }
    }
}
