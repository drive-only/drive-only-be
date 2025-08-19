package drive_only.drive_only_server.service.course;

import drive_only.drive_only_server.domain.*;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.course.create.CourseCreateForm;
import drive_only.drive_only_server.dto.course.create.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.create.CourseCreateResponse;
import drive_only.drive_only_server.dto.course.delete.CourseDeleteResponse;
import drive_only.drive_only_server.dto.course.detailSearch.CourseDetailSearchResponse;
import drive_only.drive_only_server.dto.course.search.CourseSearchRequest;
import drive_only.drive_only_server.dto.course.search.CourseSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.create.CoursePlaceCreateRequest;
import drive_only.drive_only_server.dto.coursePlace.update.CoursePlaceUpdateResponse;
import drive_only.drive_only_server.dto.like.course.CourseLikeResponse;
import drive_only.drive_only_server.dto.meta.Meta;
import drive_only.drive_only_server.dto.photo.PhotoRequest;
import drive_only.drive_only_server.dto.report.ReportResponse;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.custom.CourseNotFoundException;
import drive_only.drive_only_server.exception.custom.OwnerMismatchException;
import drive_only.drive_only_server.exception.custom.PlaceNotFoundException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.repository.category.CategoryRepository;
import drive_only.drive_only_server.repository.course.LikedCourseRepository;
import drive_only.drive_only_server.repository.coursePlace.CoursePlaceRepository;
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.repository.hide.HiddenCourseRepository;
import drive_only.drive_only_server.repository.member.MemberRepository;
import drive_only.drive_only_server.repository.photo.PhotoRepository;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import drive_only.drive_only_server.repository.tag.TagRepository;
import drive_only.drive_only_server.security.LoginMemberProvider;
import drive_only.drive_only_server.service.photo.PhotoService;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final PlaceRepository placeRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PhotoRepository photoRepository;
    private final LoginMemberProvider loginMemberProvider;
    private final LikedCourseRepository likedCourseRepository;
    private final MemberRepository memberRepository;
    private final PhotoService photoService;
    private final HiddenCourseRepository hiddenCourseRepository;

    @Transactional
    public CourseCreateResponse createCourseFromMultipart(CourseCreateForm form, List<MultipartFile> photos) {
        Member loginMember = loginMemberProvider.getLoginMember();
        CourseCreateRequest request = buildCreateRequestFromMultipart(form, photos, loginMember);
        return createCourse(request);
    }

    // 목록 조회에서 viewerId 반영
    public PaginatedResponse<CourseSearchResponse> searchCourses(CourseSearchRequest request, int page, int size) {
        validateSearchRequest(request);
        Long viewerId = null;
        Member viewer = loginMemberProvider.getLoginMemberIfExists();
        if (viewer != null) viewerId = viewer.getId();

        Page<Course> courses = courseRepository.searchCourses(request, PageRequest.of(page, size), viewerId);

        List<CourseSearchResponse> responses = courses.stream()
                .map(CourseSearchResponse::from)
                .toList();
        Meta meta = Meta.from(courses);
        return new PaginatedResponse<>(responses, meta);
    }

    // 상세 조회에서 숨김이면 Not Found 처리
    public PaginatedResponse<CourseDetailSearchResponse> searchCourseDetail(Long courseId) {
        Course course = findCourse(courseId);
        Member loginMember = loginMemberProvider.getLoginMemberIfExists();
        if (loginMember != null && hiddenCourseRepository.existsByCourseAndMember(course, loginMember)) {
            throw new CourseNotFoundException(); // 숨김이면 존재하지 않는 것처럼
        }
        List<CoursePlace> coursePlaces = coursePlaceRepository.findByCourse(course);
        CourseDetailSearchResponse response = CourseDetailSearchResponse.from(course, coursePlaces, loginMember);
        return new PaginatedResponse<>(List.of(response), null);
    }

    @Transactional
    public CoursePlaceUpdateResponse updateCourseFromMultipart(Long courseId, CourseCreateForm form, List<MultipartFile> photos) {
        Member loginMember = loginMemberProvider.getLoginMember();
        CourseCreateRequest request = buildCreateRequestFromMultipart(form, photos, loginMember);
        return updateCourse(courseId, request);
    }

    @Transactional
    public CourseDeleteResponse deleteCourse(Long courseId) {
        Course course = findCourse(courseId);
        validateCourseOwner(course);
        courseRepository.delete(course);
        return new CourseDeleteResponse(courseId);
    }

    @Transactional
    public CourseLikeResponse toggleCourseLike(Long courseId, Member member) {
        Course course = courseRepository.findById(courseId).orElseThrow(CourseNotFoundException::new);

        Optional<LikedCourse> liked = likedCourseRepository.findByCourseAndMember(course, member);

        boolean isLiked;
        if (liked.isPresent()) {
            likedCourseRepository.delete(liked.get());
            course.decreaseLikeCount();
            isLiked = false;
        } else {
            likedCourseRepository.save(new LikedCourse(member, course));
            course.increaseLikeCount();
            isLiked = true;
        }

        return new CourseLikeResponse(
                isLiked ? "게시글에 좋아요를 눌렀습니다." : "게시글의 좋아요를 취소했습니다.",
                course.getLikeCount(),
                isLiked
        );
    }

    private CourseCreateRequest buildCreateRequestFromMultipart(CourseCreateForm form, List<MultipartFile> files, Member loginMember) {
        List<String> order = form.photoKeyOrder() == null ? List.of() : form.photoKeyOrder();
        List<MultipartFile> safeFiles = files == null ? List.of() : files;
        if (order.size() != safeFiles.size()) {
            throw new BusinessException(ErrorCode.INVALID_PHOTO_MAPPING);
        }

        Map<String, String> keyToUrl = new LinkedHashMap<>();
        for (int i = 0; i < safeFiles.size(); i++) {
            String key = order.get(i).trim();
            String url = photoService.uploadFile(safeFiles.get(i), loginMember.getEmail());
            keyToUrl.put(key, url);
        }
        return buildCreateRequestWithUrls(form, keyToUrl);
    }

    private CourseCreateRequest buildCreateRequestWithUrls(CourseCreateForm form, Map<String, String> keyToUrl) {
        List<CoursePlaceCreateRequest> places = form.coursePlaces().stream()
                .map(coursePlaceDraft -> {
                    List<PhotoRequest> photoUrls = (coursePlaceDraft.photoKeys() == null ? List.<String>of() : coursePlaceDraft.photoKeys())
                            .stream()
                            .map(k -> {
                                String url = keyToUrl.get(k.trim());
                                if (url == null) throw new BusinessException(ErrorCode.INVALID_PHOTO_MAPPING);
                                return new PhotoRequest(url);
                            })
                            .toList();

                    return new CoursePlaceCreateRequest(
                            coursePlaceDraft.placeId(),
                            coursePlaceDraft.content(),
                            photoUrls,
                            coursePlaceDraft.sequence()
                    );
                })
                .toList();

        return new CourseCreateRequest(
                form.region(),
                form.subRegion(),
                form.time(),
                form.season(),
                form.theme(),
                form.areaType(),
                form.title(),
                places,
                form.tags(),
                form.recommendation(),
                form.difficulty(),
                form.isPrivate()
        );
    }

    private CourseCreateResponse createCourse(CourseCreateRequest request) {
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
        return new CourseCreateResponse(course.getId());
    }

    private CoursePlaceUpdateResponse updateCourse(Long courseId, CourseCreateRequest request) {
        Course course = findCourse(courseId);
        validateCourseOwner(course);
        Category newCategory = getCategory(request);
        List<CoursePlace> newCoursePlaces = createCoursePlaces(request);
        List<Tag> newTags = getTags(request);
        course.update(request, newCategory, newCoursePlaces, newTags);
        return new CoursePlaceUpdateResponse(course.getId());
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
                place.getName(),
                getType(place.getContentTypeId()),
                request.content(),
                photos,
                request.sequence(),
                place
        );
        coursePlaceRepository.save(coursePlace);
        return coursePlace;
    }

    private List<Photo> createPhotos(CoursePlaceCreateRequest request) {
        if (request.photoUrls() == null || request.photoUrls().isEmpty()) {
            return List.of();
        }
        if (request.photoUrls().size() > 5) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_PLACE_PHOTOS);
        }
        List<Photo> photos = request.photoUrls().stream()
                .map(photoRequest -> Photo.create(photoRequest.photoUrl()))
                .toList();
        photoRepository.saveAll(photos);
        return photos;
    }

    private String getType(int contentTypeId) {
        return switch (contentTypeId) {
            case 12, 14, 38 -> "tourist-spot";
            case 39 -> "restaurant";
            default -> "";
        };
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

    private void validateCourseOwner(Course course) {
        Member loginMember = loginMemberProvider.getLoginMember();
        if (!course.isWrittenBy(loginMember)) {
            throw new OwnerMismatchException();
        }
    }

    private void validateSearchRequest(CourseSearchRequest request) {
        boolean hasPlaceId = isNotNull(request.placeId());
        boolean hasKeyword = isNotBlank(request.keyword());
        boolean hasCategory = hasCategoryFields(request);

        if ((hasPlaceId && hasKeyword) || (hasPlaceId && hasCategory)) {
            throw new BusinessException(ErrorCode.PLACE_ID_WITH_ANYTHING_NOT_ALLOWED);
        }
        if (hasKeyword && hasCategory) {
            throw new BusinessException(ErrorCode.KEYWORD_WITH_CATEGORY_NOT_ALLOWED);
        }
        if (request.memberId() != null && memberRepository.findById(request.memberId()).isEmpty()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private boolean isNotNull(Long value) {
        return value != null;
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

    // 신고(숨김) 등록
    @Transactional
    public ReportResponse reportCourse(Long courseId) {
        Course course = findCourse(courseId);
        Member member = loginMemberProvider.getLoginMember();

        boolean exists = hiddenCourseRepository.existsByCourseAndMember(course, member);
        if (!exists) {
            hiddenCourseRepository.save(new HiddenCourse(member, course));
            return new ReportResponse("게시글을 신고하여 숨김 처리했습니다.", true, true); // 201
        }
        return new ReportResponse("이미 숨김 처리된 게시글입니다.", true, false);       // 200
    }

    // 신고(숨김) 해제 (선택)
    @Transactional
    public ReportResponse unreportCourse(Long courseId) {
        Course course = findCourse(courseId);
        Member member = loginMemberProvider.getLoginMember();

        hiddenCourseRepository.deleteByCourseAndMember(course, member); // 없으면 no-op
        return new ReportResponse("게시글 숨김을 해제했습니다.", false, false);          // 200
    }
}
