package drive_only.drive_only_server.service.data;

import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.data.AreaCodeResponse;
import drive_only.drive_only_server.dto.data.DetailIntroResponse;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse;
import drive_only.drive_only_server.dto.data.PlaceDataInitResponse.Item;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import drive_only.drive_only_server.service.client.TourApiClient;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PlaceDataService {
    private final static int DEFAULT_ROWS = 100;
    private final static int PLACE_START_PAGE = 15;
    private final static int PLACE_TOTAL_PAGES = 21;

    private final TourApiClient tourApiClient;
    private final PlaceRepository placeRepository;
    private final Map<String, String> regionCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> subRegionCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initRegionCache() {
        List<AreaCodeResponse.Item> regions = tourApiClient.fetchAreaCodes(null);
        cacheRegions(regions);
        regionCache.keySet().forEach(this::cacheSubRegions);
    }

    @Transactional
    public void importAllPlaces() {
        for (int pageNo = PLACE_START_PAGE; pageNo <= PLACE_TOTAL_PAGES; pageNo++) {
            List<PlaceDataInitResponse.Item> places = tourApiClient.fetchPlaces(pageNo, DEFAULT_ROWS);
            savePlaces(places);
        }
    }

    @Transactional
    @Scheduled(cron = "0 50 4 * * *")
    public void syncAllPlaces() {
        for (int pageNo = PLACE_START_PAGE; pageNo <= PLACE_TOTAL_PAGES; pageNo++) {
            List<PlaceDataInitResponse.Item> places = tourApiClient.fetchPlaces(pageNo, DEFAULT_ROWS);
            places.forEach(this::syncPlace);
        }
    }

    private void cacheRegions(List<AreaCodeResponse.Item> response) {
        for (AreaCodeResponse.Item item : response) {
            regionCache.put(item.code().trim(), item.name().trim());
        }
    }

    private void cacheSubRegions(String regionCode) {
        List<AreaCodeResponse.Item> subRegions = tourApiClient.fetchAreaCodes(regionCode);
        Map<String, String> subRegionsMap = new ConcurrentHashMap<>();
        for (AreaCodeResponse.Item item : subRegions) {
            subRegionsMap.put(item.code().trim(), item.name().trim());
        }
        subRegionCache.put(regionCode, subRegionsMap);
    }

    private void savePlaces(List<PlaceDataInitResponse.Item> places) {
        for (PlaceDataInitResponse.Item place : places) {
            DetailIntroResponse.Item placeDetail = tourApiClient.fetchPlaceDetail(place.contentid(), place.contenttypeid());
            if (placeDetail == null) {
                continue;
            }
            Place newPlace = createPlace(place, placeDetail);
            placeRepository.save(newPlace);
        }
    }

    private void syncPlace(Item place) {
        DetailIntroResponse.Item placeDetail = tourApiClient.fetchPlaceDetail(place.contentid(), place.contenttypeid());
        if (placeDetail == null) {
            return;
        }
        Optional<Place> findPlace = placeRepository.findByContentId(Integer.parseInt(place.contentid()));
        if (findPlace.isPresent()) {
            Place existingPlace = findPlace.get();
            updateExistingPlace(existingPlace, place, placeDetail);
        } else {
            Place newPlace = createPlace(place, placeDetail);
            placeRepository.save(newPlace);
        }
    }

    private Place createPlace(Item place, DetailIntroResponse.Item placeDetail) {
        int contentId = Integer.parseInt(place.contentid());
        int contentTypeId = Integer.parseInt(place.contenttypeid());

        return new Place(
                contentId, contentTypeId,
                place.title(), place.addr1() + " " + place.addr2(),
                getRegion(place.lDongRegnCd()), getSubRegion(place.lDongRegnCd(), place.lDongSignguCd()),
                place.firstimage2(),
                getUseTime(contentTypeId, placeDetail), getRestDate(contentTypeId, placeDetail),
                place.tel(), Double.parseDouble(place.mapx()), Double.parseDouble(place.mapy())
        );
    }

    private void updateExistingPlace(Place existingPlace, Item place, DetailIntroResponse.Item placeDetail) {
        int contentTypeId = Integer.parseInt(place.contenttypeid());

        existingPlace.updateBasicInfo(
                place.title(), place.addr1() + " " + place.addr2(),
                getRegion(place.lDongRegnCd()), getSubRegion(place.lDongRegnCd(), place.lDongSignguCd()),
                place.firstimage2(), place.tel(),
                Double.parseDouble(place.mapx()), Double.parseDouble(place.mapy())
        );

        existingPlace.updateDetailInfo(
                getUseTime(contentTypeId, placeDetail),
                getRestDate(contentTypeId, placeDetail)
        );
    }

    private String getRegion(String lDongRegnCd) {
        return regionCache.get(lDongRegnCd);
    }

    private String getSubRegion(String lDongRegnCd, String lDongSignguCd) {
        Map<String, String> subRegionMap = subRegionCache.get(lDongRegnCd);
        return (subRegionMap != null) ? subRegionMap.get(lDongSignguCd) : null;
    }

    private String getUseTime(int typeId, DetailIntroResponse.Item item) {
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

    private String getRestDate(int typeId, DetailIntroResponse.Item item) {
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
