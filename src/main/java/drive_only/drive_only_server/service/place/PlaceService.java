package drive_only.drive_only_server.service.place;

import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.meta.Meta;
import drive_only.drive_only_server.dto.place.PlaceSearchRequest;
import drive_only.drive_only_server.dto.place.PlaceSearchResponse;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;

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

}
