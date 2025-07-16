package drive_only.drive_only_server.service.place;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.domain.SavedPlace;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.meta.Meta;
import drive_only.drive_only_server.dto.place.myPlace.SavePlaceResponse;
import drive_only.drive_only_server.dto.place.nearbySearch.NearbyPlaceTourApiResponse;
import drive_only.drive_only_server.dto.place.nearbySearch.NearbyPlaceTourApiResponse.Item;
import drive_only.drive_only_server.dto.place.nearbySearch.NearbyPlacesResponse;
import drive_only.drive_only_server.dto.place.search.PlaceSearchRequest;
import drive_only.drive_only_server.dto.place.search.PlaceSearchResponse;
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.repository.course.SavedPlaceRepository;
import drive_only.drive_only_server.repository.member.MemberRepository;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import drive_only.drive_only_server.security.LoginMemberProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceService {
    @Value("${tourapi.service-key}")
    private String tourApiServiceKey;
    private final WebClient webClient;
    private final PlaceRepository placeRepository;
    private final CourseRepository courseRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final LoginMemberProvider loginMemberProvider;
    private static final int DEFAULT_RADIUS = 3000;
    private static final int DEFAULT_PAGE_NO = 1;

    public PaginatedResponse<PlaceSearchResponse> searchPlaces(PlaceSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Place> places = placeRepository.searchPlaces(request, pageable);
        List<PlaceSearchResponse> responses = places.stream()
                .map(this::createPlaceSearchResponse)
                .toList();

        Meta meta = new Meta(
                (int) places.getTotalElements(),
                places.getNumber() + 1,
                places.getSize(),
                places.hasNext()
        );

        return new PaginatedResponse<>(responses, meta);
    }

    public PaginatedResponse<NearbyPlacesResponse> searchNearbyPlaces(Long courseId, String type) {
        Course course = findCourse(courseId);
        List<CoursePlace> coursePlaces = course.getCoursePlaces();
        int coursePlaceCount = coursePlaces.size();
        List<Integer> distribution = getDistribution(coursePlaceCount);
        int contentTypeId = getContentTypeId(type);
        List<NearbyPlacesResponse> results = new ArrayList<>();

        for (int i = 0; i < coursePlaces.size(); i++) {
            results.add(createNearbyPlacesResponse(coursePlaces.get(i), contentTypeId, distribution.get(i)));
        }
        return new PaginatedResponse<>(results, null);
    }

    public PaginatedResponse<PlaceSearchResponse> searchSavedPlaces() {
        Member member = loginMemberProvider.getLoginMember()
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다."));
        List<SavedPlace> savedPlaces = savedPlaceRepository.findByMember(member);
        List<PlaceSearchResponse> results = savedPlaces.stream()
                .map(savedPlace -> {
                    Place place = savedPlace.getPlace();
                    return createPlaceSearchResponse(place);
                })
                .toList();
        return new PaginatedResponse<>(results, null);
    }

    @Transactional
    public SavePlaceResponse savePlace(Long placeId) {
        Member member = loginMemberProvider.getLoginMember()
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다."));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다."));

        SavedPlace savedPlace = savedPlaceRepository.save(new SavedPlace(member, place));
        return new SavePlaceResponse(savedPlace.getId(), "해당 장소가 성공적으로 저장되었습니다.");
    }

    private List<Integer> getDistribution(int coursePlaceCount) {
        return switch (coursePlaceCount) {
            case 1 -> List.of(5);
            case 2 -> List.of(3, 2);
            case 3 -> List.of(2, 2, 1);
            case 4 -> List.of(2, 1, 1, 1);
            case 5 -> List.of(1, 1, 1, 1, 1);
            default -> throw new IllegalArgumentException("코스는 1개 이상 5개 이하이어야 합니다. coursePlaceCount: " + coursePlaceCount);
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

        return new NearbyPlacesResponse(
                coursePlace.getId(),
                coursePlace.getPlace().getId(),
                coursePlace.getPlace().getName(),
                searchResponses
        );
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
                .map(place -> {
                    Place findPlace = findPlace(place);
                    return createPlaceSearchResponse(findPlace);
                })
                .toList();
    }

    private PlaceSearchResponse createPlaceSearchResponse(Place place) {
        return new PlaceSearchResponse(
                place.getId(),
                getType(place.getContentTypeId()),
                place.getName(),
                place.getThumbNailUrl(),
                place.getUseTime(),
                place.getRestDate(),
                place.getPhoneNum(),
                place.getLat(),
                place.getLng()
        );
    }

    private String getType(int contentTypeId) {
        if (contentTypeId == 12 || contentTypeId == 14 || contentTypeId ==38) {
            return "tourist-spot";
        }
        if (contentTypeId == 39) {
            return "restaurant";
        }
        return "";
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스를 찾을 수 없습니다."));
    }

    private Place findPlace(Item place) {
        return placeRepository.findByContentId(Integer.parseInt(place.contentid()))
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다."));
    }
}
