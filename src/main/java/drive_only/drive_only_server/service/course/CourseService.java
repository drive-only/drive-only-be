package drive_only.drive_only_server.service.course;

import drive_only.drive_only_server.domain.Category;
import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.Photo;
import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.domain.Tag;
import drive_only.drive_only_server.dto.category.CategoryResponse;
import drive_only.drive_only_server.dto.course.create.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.create.CourseCreateResponse;
import drive_only.drive_only_server.dto.course.detailSearch.CourseDetailSearchResponse;
import drive_only.drive_only_server.dto.coursePlace.create.CoursePlaceCreateRequest;
import drive_only.drive_only_server.dto.coursePlace.search.CoursePlaceSearchResponse;
import drive_only.drive_only_server.dto.photo.PhotoRequest;
import drive_only.drive_only_server.dto.photo.PhotoResponse;
import drive_only.drive_only_server.dto.tag.TagRequest;
import drive_only.drive_only_server.dto.tag.TagResponse;
import drive_only.drive_only_server.repository.CategoryRepository;
import drive_only.drive_only_server.repository.CoursePlaceRepository;
import drive_only.drive_only_server.repository.CourseRepository;
import drive_only.drive_only_server.repository.MemberRepository;
import drive_only.drive_only_server.repository.PhotoRepository;
import drive_only.drive_only_server.repository.PlaceRepository;
import drive_only.drive_only_server.repository.TagRepository;
import java.time.LocalDate;
import java.util.ArrayList;
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
        Member testMember = createMember();
        Category category = createCategory(request);
        List<Tag> tags = createTag(request);
        Course course = createCourse(request, testMember, coursePlaces, category, tags);
        return new CourseCreateResponse(course.getId(), "게시글이 성공적으로 등록되었습니다.");
    }

    public CourseDetailSearchResponse searchCourseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 코스(게시글)을 찾을 수 없습니다."));
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

    private List<TagResponse> createTagResponse(Course course) {
        List<TagResponse> tagResponses = new ArrayList<>();
        for (Tag tag : course.getTags()) {
            tagResponses.add(new TagResponse(tag.getId(), tag.getName()));
        }
        return tagResponses;
    }

    private List<CoursePlaceSearchResponse> createCoursePlaceSearchResponse(List<CoursePlace> coursePlaces) {
        List<CoursePlaceSearchResponse> coursePlaceSearchResponses = new ArrayList<>();
        for (CoursePlace coursePlace : coursePlaces) {
            coursePlaceSearchResponses.add(
                    new CoursePlaceSearchResponse(
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
                    ));
        }
        return coursePlaceSearchResponses;
    }

    private List<PhotoResponse> createPhotoResponse(CoursePlace coursePlace) {
        List<PhotoResponse> photoResponses = new ArrayList<>();
        for (Photo photo : coursePlace.getPhotos()) {
            photoResponses.add(new PhotoResponse(photo.getId(), photo.getUrl()));
        }
        return photoResponses;
    }

    private List<CoursePlace> createCoursePlaces(CourseCreateRequest request) {
        List<CoursePlace> coursePlaces = new ArrayList<>();
        for (CoursePlaceCreateRequest coursePlaceCreateRequest : request.getCoursePlaces()) {
            Place place = findPlace(coursePlaceCreateRequest);
            CoursePlace coursePlace = createCoursePlace(coursePlaceCreateRequest, place);
            coursePlaces.add(coursePlace);
        }
        return coursePlaces;
    }

    private Place findPlace(CoursePlaceCreateRequest coursePlaceCreateRequest) {
        return placeRepository.findById(Long.parseLong(coursePlaceCreateRequest.getPlaceId()))
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다."));
    }

    private CoursePlace createCoursePlace(CoursePlaceCreateRequest coursePlaceCreateRequest, Place place) {
        List<Photo> photos = new ArrayList<>();
        for (PhotoRequest photoRequest : coursePlaceCreateRequest.getPhotoUrls()) {
            Photo photo = new Photo(photoRequest.getPhotoUrl());
            photoRepository.save(photo);
            photos.add(photo);
        }

        CoursePlace coursePlace = new CoursePlace(
                coursePlaceCreateRequest.getName(),
                coursePlaceCreateRequest.getPlaceType(),
                coursePlaceCreateRequest.getContent(),
                photos,
                coursePlaceCreateRequest.getSequence(),
                place
        );
        coursePlaceRepository.save(coursePlace);
        return coursePlace;
    }

    private Member createMember() {
        Member mockMember = new Member("email", "nickname", "url", "provider");
        memberRepository.save(mockMember);
        return mockMember;
    }

    private Category createCategory(CourseCreateRequest request) {
        Category category = new Category(
                request.getRegion(),
                request.getSubRegion(),
                request.getTime(),
                request.getSeason(),
                request.getTheme(),
                request.getAreaType()
        );
        return categoryRepository.save(category);
    }

    private List<Tag> createTag(CourseCreateRequest request) {
        List<Tag> tagResponse = new ArrayList<>();
        for (TagRequest tagRequest : request.getTags()) {
            Tag tag = new Tag(tagRequest.getTagName());
            tagRepository.save(tag);
            tagResponse.add(tag);
        }
        return tagResponse;
    }

    private Course createCourse(CourseCreateRequest request, Member testMember, List<CoursePlace> coursePlaces, Category category, List<Tag> tags) {
        Course course = Course.createCourse(
                request.getTitle(),
                LocalDate.now(),
                request.getRecommendation(), request.getDifficulty(),
                0, 0, 0, false,
                testMember, //TODO : 나중에 Member 생성되면 LoginMember(현재 로그인 된 사용자)로 변경
                category,
                coursePlaces,
                tags
        );
        return courseRepository.save(course);
    }
}
