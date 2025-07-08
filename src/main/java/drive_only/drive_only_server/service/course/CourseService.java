package drive_only.drive_only_server.service.course;

import drive_only.drive_only_server.domain.*;
import drive_only.drive_only_server.dto.category.CategoryResponse;
import drive_only.drive_only_server.dto.course.create.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.create.CourseCreateResponse;
import drive_only.drive_only_server.dto.course.delete.CourseDeleteResponse;
import drive_only.drive_only_server.dto.course.detailSearch.CourseDetailSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.create.CoursePlaceCreateRequest;
import drive_only.drive_only_server.dto.coursePlace.search.CoursePlaceSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.update.CoursePlaceUpdateResponse;
import drive_only.drive_only_server.dto.photo.PhotoResponse;
import drive_only.drive_only_server.dto.tag.TagResponse;
import drive_only.drive_only_server.repository.CategoryRepository;
import drive_only.drive_only_server.repository.CoursePlaceRepository;
import drive_only.drive_only_server.repository.CourseRepository;
import drive_only.drive_only_server.repository.MemberRepository;
import drive_only.drive_only_server.repository.PhotoRepository;
import drive_only.drive_only_server.repository.PlaceRepository;
import drive_only.drive_only_server.repository.TagRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final PlaceRepository placeRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PhotoRepository photoRepository;

    @Transactional
    public CourseCreateResponse createCourse(CourseCreateRequest request) {
        List<CoursePlace> coursePlaces = createCoursePlaces(request);
        Member member = createMember();
        Category category = createCategory(request);
        List<Tag> tags = createTag(request);
        Course course = createCourse(request, member, coursePlaces, category, tags);
        return new CourseCreateResponse(course.getId(), "게시글이 성공적으로 등록되었습니다.");
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
        courseRepository.deleteById(courseId);
        return new CourseDeleteResponse(courseId, "게시글이 성공적으로 삭제되었습니다.");
    }

    private Course createCourse(CourseCreateRequest request, Member member, List<CoursePlace> coursePlaces, Category category, List<Tag> tags) {
        Course course = Course.createCourse(
                request.title(),
                LocalDate.now(),
                request.recommendation(), request.difficulty(),
                0, 0, 0, false,
                member, //TODO : 나중에 로그인, 회원가입 기능이 완성되면 LoginMember(현재 로그인 된 사용자)로 변경, 현재는 테스트용 멤버
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

    private Member createMember() {
        Member mockMember = Member.createMember(
                "email",
                "nickname",
                "url",
                ProviderType.KAKAO
        );
        memberRepository.save(mockMember);
        return mockMember;
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
