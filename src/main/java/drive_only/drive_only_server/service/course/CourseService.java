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
import drive_only.drive_only_server.s3.S3ImageStorageProvider;
import drive_only.drive_only_server.security.LoginMemberProvider;
import drive_only.drive_only_server.service.photo.PhotoService;
import java.time.LocalDate;
import java.util.*;

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
    private final S3ImageStorageProvider s3Provider;

    @Transactional
    public CourseCreateResponse createCourseFromMultipart(CourseCreateForm form, List<MultipartFile> photos) {
        Member loginMember = loginMemberProvider.getLoginMember();
        List<String> order = form.photoKeyOrder() == null ? List.of() : form.photoKeyOrder();
        prevalidatePhotoMapping(form, order);

        Map<String, PhotoService.UploadedPhoto> uploaded =
                photoService.uploadManyInOrder(order, photos, loginMember.getEmail());

        List<String> rollbackKeys = uploaded.values().stream()
                .map(PhotoService.UploadedPhoto::getS3Key)
                .toList();

        try {
            CourseCreateRequest request = buildCreateRequestWithUploaded(form, uploaded);
            return createCourse(request);
        } catch (RuntimeException e) {
            s3Provider.deleteQuietly(rollbackKeys);
            throw e;
        }
    }

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

    @Transactional
    public PaginatedResponse<CourseDetailSearchResponse> searchCourseDetail(Long courseId) {
        Course course = findCourseWithMember(courseId);
        course.increaseViewCount();
        Member loginMember = loginMemberProvider.getLoginMemberIfExists();
        if (loginMember != null && hiddenCourseRepository.existsByCourseAndMember(course, loginMember)) {
            throw new CourseNotFoundException();
        }
        List<CoursePlace> coursePlaces = coursePlaceRepository.findByCourse(course);
        CourseDetailSearchResponse response = CourseDetailSearchResponse.from(course, coursePlaces, loginMember);
        return new PaginatedResponse<>(List.of(response), null);
    }

    @Transactional
    public CoursePlaceUpdateResponse updateCourseFromMultipart(Long courseId, CourseCreateForm form, List<MultipartFile> photos) {
        Member loginMember = loginMemberProvider.getLoginMember();

        List<String> order = form.photoKeyOrder() == null ? List.of() : form.photoKeyOrder();
        prevalidatePhotoMapping(form, order);

        Course course = findCourse(courseId);
        validateCourseOwner(course);
        List<CoursePlace> beforeCps = coursePlaceRepository.findByCourse(course);
        Set<String> beforeKeys = beforeCps.stream()
                .flatMap(cp -> cp.getPhotos().stream())
                .map(Photo::getS3Key)
                .collect(java.util.stream.Collectors.toSet());

        Map<String, PhotoService.UploadedPhoto> uploaded =
                photoService.uploadManyInOrder(order, photos, loginMember.getEmail());
        List<String> rollbackKeys = uploaded.values().stream().map(PhotoService.UploadedPhoto::getS3Key).toList();

        try {
            CourseCreateRequest request = buildCreateRequestWithUploaded(form, uploaded);
            CoursePlaceUpdateResponse res = updateCourse(courseId, request);
            Course updated = findCourse(courseId);
            List<CoursePlace> afterCps = coursePlaceRepository.findByCourse(updated);
            Set<String> afterKeys = afterCps.stream()
                    .flatMap(cp -> cp.getPhotos().stream())
                    .map(Photo::getS3Key)
                    .collect(java.util.stream.Collectors.toSet());
            beforeKeys.removeAll(afterKeys);
            if (!beforeKeys.isEmpty()) s3Provider.deleteQuietly(beforeKeys);
            return res;
        } catch (RuntimeException e) {
            s3Provider.deleteQuietly(rollbackKeys);
            throw e;
        }
    }

    @Transactional
    public CourseDeleteResponse deleteCourse(Long courseId) {
        Course course = findCourse(courseId);
        validateCourseOwner(course);
        List<CoursePlace> cps = coursePlaceRepository.findByCourse(course);
        List<String> keys = cps.stream()
                .flatMap(cp -> cp.getPhotos().stream())
                .map(Photo::getS3Key)
                .toList();

        s3Provider.deleteQuietly(keys);
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

    @Transactional
    public ReportResponse reportCourse(Long courseId) {
        Course course = findCourse(courseId);
        Member member = loginMemberProvider.getLoginMember();

        boolean exists = hiddenCourseRepository.existsByCourseAndMember(course, member);
        if (!exists) {
            hiddenCourseRepository.save(new HiddenCourse(member, course));
            return new ReportResponse("게시글을 신고하여 숨김 처리했습니다.", true, true);
        }
        return new ReportResponse("이미 숨김 처리된 게시글입니다.", true, false);
    }

    @Transactional
    public ReportResponse unreportCourse(Long courseId) {
        Course course = findCourse(courseId);
        Member member = loginMemberProvider.getLoginMember();

        hiddenCourseRepository.deleteByCourseAndMember(course, member);
        return new ReportResponse("게시글 숨김을 해제했습니다.", false, false);
    }

    private CourseCreateRequest buildCreateRequestWithUploaded(CourseCreateForm form, Map<String, PhotoService.UploadedPhoto> uploaded) {
        int total = form.coursePlaces()==null ? 0 :
                form.coursePlaces().stream().mapToInt(cp -> cp.photoKeys()==null?0:cp.photoKeys().size()).sum();
        if (total > 50) throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);

        List<CoursePlaceCreateRequest> places = form.coursePlaces().stream()
                .map(draft -> {
                    List<String> keys = (draft.photoKeys()==null) ? List.of() : draft.photoKeys();
                    if (keys.size() > 10) throw new BusinessException(ErrorCode.INVALID_COURSE_PLACE_PHOTOS);

                    List<PhotoRequest> photoDtos = keys.stream()
                            .map(k -> {
                                PhotoService.UploadedPhoto up = uploaded.get(k.trim());
                                if (up == null) throw new BusinessException(ErrorCode.INVALID_PHOTO_MAPPING);
                                return new PhotoRequest(up.getS3Key(), up.getCdnUrl()); // ★ 둘 다
                            })
                            .toList();

                    return new CoursePlaceCreateRequest(
                            draft.placeId(), draft.content(), photoDtos, draft.sequence());
                })
                .toList();

        return new CourseCreateRequest(
                form.region(), form.subRegion(), form.time(), form.season(),
                form.theme(), form.areaType(), form.title(),
                places, form.tags(), form.recommendation(), form.difficulty(), form.isPrivate()
        );
    }

    private void prevalidatePhotoMapping(CourseCreateForm form, List<String> order) {
        int total = form.coursePlaces() == null ? 0 :
                form.coursePlaces().stream().mapToInt(cp -> cp.photoKeys()==null?0:cp.photoKeys().size()).sum();
        if (total > 50) throw new BusinessException(ErrorCode.INVALID_IMAGE_DATA);

        Set<String> orderSet = new java.util.HashSet<>();
        for (String k : (order == null ? List.<String>of() : order)) {
            orderSet.add(k.trim());
        }

        if (form.coursePlaces() != null) {
            for (var cp : form.coursePlaces()) {
                List<String> keys = (cp.photoKeys() == null) ? List.of() : cp.photoKeys();
                if (keys.size() > 10) {
                    throw new BusinessException(ErrorCode.INVALID_COURSE_PLACE_PHOTOS);
                }
                for (String k : keys) {
                    String kk = (k == null) ? "" : k.trim();
                    if (!orderSet.contains(kk)) {
                        throw new BusinessException(ErrorCode.INVALID_PHOTO_MAPPING);
                    }
                }
            }
        }
    }

    private CourseCreateResponse createCourse(CourseCreateRequest request) {
        List<CoursePlace> coursePlaces = createCoursePlaces(request);
        Member loginMember = loginMemberProvider.getLoginMember();
        Category category = getCategory(request);
        List<Tag> tags = getTags(request);
        Course course = Course.createCourse(
                request.title(), LocalDate.now(), request.recommendation(), request.difficulty(),
                0, 0, 0, false, request.isPrivate(),
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
        CoursePlace coursePlace = CoursePlace.createCoursePlace(
                place.getName(),
                getType(place.getContentTypeId()),
                request.content(),
                photos,
                request.sequence(),
                place);
        coursePlaceRepository.save(coursePlace);
        return coursePlace;
    }

    private List<Photo> createPhotos(CoursePlaceCreateRequest request) {
        if (request.photoUrls() == null || request.photoUrls().isEmpty()) return List.of();
        if (request.photoUrls().size() > 10) throw new BusinessException(ErrorCode.INVALID_COURSE_PLACE_PHOTOS);

        List<Photo> photos = request.photoUrls().stream()
                .map(p -> Photo.create(p.s3Key(), p.photoUrl())) // ★ 변경
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

    private Course findCourseWithMember(Long courseId) {
        return courseRepository.findWithMemberById(courseId).orElseThrow(CourseNotFoundException::new);
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
}
