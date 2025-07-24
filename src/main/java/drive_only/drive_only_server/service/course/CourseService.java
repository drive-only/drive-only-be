package drive_only.drive_only_server.service.course;

import drive_only.drive_only_server.domain.*;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.course.create.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.create.CourseCreateResponse;
import drive_only.drive_only_server.dto.course.delete.CourseDeleteResponse;
import drive_only.drive_only_server.dto.course.detailSearch.CourseDetailSearchResponse;
import drive_only.drive_only_server.dto.course.search.CourseSearchRequest;
import drive_only.drive_only_server.dto.course.search.CourseSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.create.CoursePlaceCreateRequest;
import drive_only.drive_only_server.dto.coursePlace.update.CoursePlaceUpdateResponse;
import drive_only.drive_only_server.dto.meta.Meta;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.custom.CourseNotFoundException;
import drive_only.drive_only_server.exception.custom.PlaceNotFoundException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.repository.category.CategoryRepository;
import drive_only.drive_only_server.repository.coursePlace.CoursePlaceRepository;
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.repository.photo.PhotoRepository;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import drive_only.drive_only_server.repository.tag.TagRepository;
import drive_only.drive_only_server.security.LoginMemberProvider;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseService {
    private static final String SUCCESS_CREATE = "게시글이 성공적으로 등록되었습니다.";
    private static final String SUCCESS_UPDATE = "게시글이 성공적으로 수정되었습니다.";
    private static final String SUCCESS_DELETE = "게시글이 성공적으로 삭제되었습니다.";

    private final CourseRepository courseRepository;
    private final PlaceRepository placeRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PhotoRepository photoRepository;
    private final LoginMemberProvider loginMemberProvider;

    @Transactional
    public CourseCreateResponse createCourse(CourseCreateRequest request) {
        List<CoursePlace> coursePlaces = createCoursePlaces(request);
        Member loginMember = loginMemberProvider.getLoginMember();
        Category category = getCategory(request);
        List<Tag> tags = getTags(request);

        Course course = Course.createCourse(
                request.title(), LocalDate.now(), request.recommendation(), request.difficulty(),
                0, 0, 0, false,
                loginMember, category, coursePlaces, tags
        );
        courseRepository.save(course);

        return new CourseCreateResponse(course.getId(), SUCCESS_CREATE);
    }

    public PaginatedResponse<CourseSearchResponse> searchCourses(CourseSearchRequest request, int page, int size) {
        validateSearchRequest(request);
        Page<Course> courses = courseRepository.searchCourses(request, PageRequest.of(page, size));
        List<CourseSearchResponse> responses = courses.stream()
                .map(CourseSearchResponse::from)
                .toList();
        Meta meta = Meta.from(courses);
        return new PaginatedResponse<>(responses, meta);
    }

    public CourseDetailSearchResponse searchCourseDetail(Long courseId) {
        Course course = findCourse(courseId);
        List<CoursePlace> coursePlaces = coursePlaceRepository.findByCourse(course);
        return CourseDetailSearchResponse.from(course, coursePlaces);
    }

    @Transactional
    public CoursePlaceUpdateResponse updateCourse(Long courseId, CourseCreateRequest request) {
        Course course = findCourse(courseId);
        Category newCategory = getCategory(request);
        List<CoursePlace> newCoursePlaces = createCoursePlaces(request);
        List<Tag> newTags = getTags(request);

        course.update(request, newCategory, newCoursePlaces, newTags);

        return new CoursePlaceUpdateResponse(course.getId(), SUCCESS_UPDATE);
    }

    @Transactional
    public CourseDeleteResponse deleteCourse(Long courseId) {
        Course course = findCourse(courseId);
        courseRepository.delete(course);
        return new CourseDeleteResponse(courseId, SUCCESS_DELETE);
    }

    private List<CoursePlace> createCoursePlaces(CourseCreateRequest request) {
        return request.coursePlaces().stream()
                .map(coursePlaceCreateRequest -> {
                    Place place = findPlace(Long.parseLong(coursePlaceCreateRequest.placeId()));
                    return createCoursePlace(coursePlaceCreateRequest, place);
                })
                .toList();
    }

    private CoursePlace createCoursePlace(CoursePlaceCreateRequest request, Place place) {
        List<Photo> photos = createPhotos(request);
        CoursePlace coursePlace = new CoursePlace(
                request.placeName(),
                request.placeType(),
                request.content(),
                photos,
                request.sequence(),
                place
        );
        coursePlaceRepository.save(coursePlace);
        return coursePlace;
    }

    private List<Photo> createPhotos(CoursePlaceCreateRequest request) {
        List<Photo> photos = request.photoUrls().stream()
                .map(photoRequest -> new Photo(photoRequest.photoUrl()))
                .toList();
        photoRepository.saveAll(photos);
        return photos;
    }

    private Category getCategory(CourseCreateRequest request) {
        Category category = Category.createCategory(request.region(),
                request.subRegion(),
                request.time(),
                request.season(),
                request.theme(),
                request.areaType()
        );
        return categoryRepository.save(category);
    }

    private List<Tag> getTags(CourseCreateRequest request) {
        List<Tag> tags = request.tags().stream()
                .map(tagRequest -> new Tag(tagRequest.tagName()))
                .toList();
        tagRepository.saveAll(tags);
        return tags;
    }

    private Place findPlace(Long placeId) {
        return placeRepository.findById(placeId).orElseThrow(PlaceNotFoundException::new);
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId).orElseThrow(CourseNotFoundException::new);
    }

    private void validateSearchRequest(CourseSearchRequest request) {
        boolean hasKeyword = isNotBlank(request.keyword());
        boolean hasCategory = hasCategoryFields(request);

        if (hasKeyword && hasCategory) {
            throw new BusinessException(ErrorCode.KEYWORD_WITH_CATEGORY_NOT_ALLOWED);
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasCategoryFields(CourseSearchRequest request) {
        return isNotBlank(request.region())
                || isNotBlank(request.subRegion())
                || isNotBlank(request.time())
                || isNotBlank(request.season())
                || isNotBlank(request.theme())
                || isNotBlank(request.areaType());
    }
}
