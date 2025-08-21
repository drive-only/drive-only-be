package drive_only.drive_only_server.domain;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region")
    private String region;

    @Column(name = "sub_region")
    private String subRegion;

    @Column(name = "time")
    private String time;

    @Column(name = "season")
    private String season;

    @Column(name = "theme")
    private String theme;

    @Column(name = "area_type")
    private String areaType;

    public static Category createCategory(String region, String subRegion, String time,
                                          String season, String theme, String areaType) {
        validateRequiredFields(region, season, areaType);
        Category category = new Category();
        category.region = normalizeNullable(region);
        category.subRegion = normalizeNullable(subRegion);
        category.time = normalizeNullable(time);
        category.season = normalizeNullable(season);
        category.theme = normalizeNullable(theme);
        category.areaType = normalizeNullable(areaType);
        return category;
    }

    private static void validateRequiredFields(String region, String season, String areaType) {
        if (isBlank(region))    throw new BusinessException(ErrorCode.INVALID_CATEGORY_REGION);
        if (isBlank(season))    throw new BusinessException(ErrorCode.INVALID_CATEGORY_SEASON);
        if (isBlank(areaType))  throw new BusinessException(ErrorCode.INVALID_CATEGORY_AREA_TYPE);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String normalizeNullable(String s) {
        return s == null ? null : s.trim();
    }
}
