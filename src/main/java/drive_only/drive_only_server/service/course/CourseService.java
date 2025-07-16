package drive_only.drive_only_server.service.course;

import drive_only.drive_only_server.domain.*;
import drive_only.drive_only_server.dto.category.CategoryResponse;
import drive_only.drive_only_server.dto.common.PaginatedResponse;
import drive_only.drive_only_server.dto.course.create.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.create.CourseCreateResponse;
import drive_only.drive_only_server.dto.course.delete.CourseDeleteResponse;
import drive_only.drive_only_server.dto.course.detailSearch.CourseDetailSearchResponse;
import drive_only.drive_only_server.dto.course.search.CourseSearchRequest;
import drive_only.drive_only_server.dto.course.search.CourseSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.create.CoursePlaceCreateRequest;
import drive_only.drive_only_server.dto.coursePlace.search.CoursePlaceSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.update.CoursePlaceUpdateResponse;
import drive_only.drive_only_server.dto.meta.Meta;
import drive_only.drive_only_server.dto.photo.PhotoResponse;
import drive_only.drive_only_server.dto.tag.TagResponse;
import drive_only.drive_only_server.repository.category.CategoryRepository;
import drive_only.drive_only_server.repository.coursePlace.CoursePlaceRepository;
import drive_only.drive_only_server.repository.course.CourseRepository;
import drive_only.drive_only_server.repository.member.MemberRepository;
import drive_only.drive_only_server.repository.photo.PhotoRepository;
import drive_only.drive_only_server.repository.place.PlaceRepository;
import drive_only.drive_only_server.repository.tag.TagRepository;
import drive_only.drive_only_server.security.LoginMemberProvider;
import java.time.LocalDate;
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
public class CourseService {
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
        Member loginMember = loginMemberProvider.getLoginMember()
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다."));
        Category category = createCategory(request);
        List<Tag> tags = createTag(request);
        Course course = createCourse(request, loginMember, coursePlaces, category, tags);
        return new CourseCreateResponse(course.getId(), "게시글이 성공적으로 등록되었습니다.");
    }

    public PaginatedResponse<CourseSearchResponse> searchCourses(CourseSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> courses = courseRepository.searchCourses(request, pageable);

        List<CourseSearchResponse> responses = courses.stream()
                .map(this::createCourseSearchResponse)
                .toList();

        Meta meta = new Meta(
                (int) courses.getTotalElements(),
                courses.getNumber() + 1,
                courses.getSize(),
                courses.hasNext()
        );

        return new PaginatedResponse<>(responses, meta);
    }

    public CourseDetailSearchResponse searchCourseDetail(Long courseId) {
        Course course = findCourse(courseId);
        List<CoursePlace> coursePlaces = coursePlaceRepository.findByCourse(course);

        return new CourseDetailSearchResponse(
                course.getId(),
                course.getTitle(),
                course.getMember().getProfileImageUrl(),
                course.getMember().getNickname(),
                course.getCreatedDate(),
                createCategoryResponse(course),
                createTagResponse(course),
                createCoursePlaceSearchResponse(coursePlaces),
                course.getRecommendation(),
                course.getDifficulty(),
                course.getLikeCount(),
                course.getViewCount(),
                course.isLiked()
        );
    }

    @Transactional
    public CoursePlaceUpdateResponse updateCourse(Long courseId, CourseCreateRequest request) {
        Course course = findCourse(courseId);
        Category newCategory = createCategory(request);
        List<CoursePlace> newCoursePlaces = createCoursePlaces(request);
        List<Tag> newTags = createTag(request);
        course.update(request, newCategory, newCoursePlaces, newTags);
        return new CoursePlaceUpdateResponse(course.getId(), "게시글이 성공적으로 수정되었습니다.");
    }

    @Transactional
    public CourseDeleteResponse deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
        courseRepository.delete(course);
        return new CourseDeleteResponse(courseId, "게시글이 성공적으로 삭제되었습니다.");
    }

    private Course createCourse(CourseCreateRequest request, Member member, List<CoursePlace> coursePlaces, Category category, List<Tag> tags) {
        Course course = Course.createCourse(
                request.title(),
                LocalDate.now(),
                request.recommendation(), request.difficulty(),
                0, 0, 0, false,
                member,
                category,
                coursePlaces,
                tags
        );
        return courseRepository.save(course);
    }

    private List<CoursePlace> createCoursePlaces(CourseCreateRequest request) {
        return request.coursePlaces().stream()
                .map(coursePlaceCreateRequest -> {
                    Place place = findPlace(coursePlaceCreateRequest);
                    return createCoursePlace(coursePlaceCreateRequest, place);
                })
                .toList();
    }

    private CoursePlace createCoursePlace(CoursePlaceCreateRequest request, Place place) {
        List<Photo> photos = createPhotos(request);
        CoursePlace coursePlace = new CoursePlace(
                request.name(),
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
        return request.photoUrls().stream()
                .map(photoRequest -> {
                    Photo photo = new Photo(photoRequest.photoUrl());
                    photoRepository.save(photo);
                    return photo;
                })
                .toList();
    }

    private Category createCategory(CourseCreateRequest request) {
        Category category = new Category(
                request.region(),
                request.subRegion(),
                request.time(),
                request.season(),
                request.theme(),
                request.areaType()
        );
        return categoryRepository.save(category);
    }

    private List<Tag> createTag(CourseCreateRequest request) {
        return request.tags().stream()
                .map(tagRequest -> {
                    Tag tag = new Tag(tagRequest.tagName());
                    tagRepository.save(tag);
                    return tag;
                })
                .toList();
    }

    private CourseSearchResponse createCourseSearchResponse(Course course) {
        return new CourseSearchResponse(
                course.getId(),
                course.getMember().getNickname(),
                String.valueOf(course.getCreatedDate()),
                course.getTitle(),
                getCourseThumbnailUrl(course),
                getCoursePlaceNames(course),
                createCategoryResponse(course),
                course.getLikeCount(),
                course.getViewCount()
        );
    }

    private String getCourseThumbnailUrl(Course course) {
        return course.getCoursePlaces().get(0).getPhotos().get(0).getUrl();
    }

    private List<String> getCoursePlaceNames(Course course) {
        return course.getCoursePlaces().stream()
                .map(CoursePlace::getName)
                .toList();
    }

    private List<CoursePlaceSearchResponse> createCoursePlaceSearchResponse(List<CoursePlace> coursePlaces) {
        return coursePlaces.stream()
                .map(coursePlace -> {
                    return new CoursePlaceSearchResponse(
                            coursePlace.getId(),
                            coursePlace.getPlace().getId(),
                            coursePlace.getPlaceType(),
                            coursePlace.getName(),
                            coursePlace.getPlace().getAddress(),
                            coursePlace.getContent(),
                            createPhotoResponse(coursePlace),
                            coursePlace.getPlace().getLat(),
                            coursePlace.getPlace().getLng(),
                            coursePlace.getSequence()
                    );
                })
                .toList();
    }

    private List<PhotoResponse> createPhotoResponse(CoursePlace coursePlace) {
        return coursePlace.getPhotos().stream()
                .map(photo -> new PhotoResponse(photo.getId(), photo.getUrl()))
                .toList();
    }

    private List<TagResponse> createTagResponse(Course course) {
        return course.getTags().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName()))
                .toList();
    }

    private CategoryResponse createCategoryResponse(Course course) {
        return new CategoryResponse(
                course.getCategory().getRegion(),
                course.getCategory().getSubRegion(),
                course.getCategory().getSeason(),
                course.getCategory().getTime(),
                course.getCategory().getTheme(),
                course.getCategory().getAreaType()
        );
    }

    private Place findPlace(CoursePlaceCreateRequest coursePlaceCreateRequest) {
        return placeRepository.findById(Long.parseLong(coursePlaceCreateRequest.placeId()))
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다."));
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스(게시글)을 찾을 수 없습니다."));
    }
}
