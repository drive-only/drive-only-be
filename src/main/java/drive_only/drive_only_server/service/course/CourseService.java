package drive_only.drive_only_server.service.course;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.CoursePlace;
import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.Place;
import drive_only.drive_only_server.dto.course.CourseCreateRequest;
import drive_only.drive_only_server.dto.course.CourseCreateResponse;
import drive_only.drive_only_server.dto.coursePlace.CoursePlaceCreateRequest;
import drive_only.drive_only_server.repository.CoursePlaceRepository;
import drive_only.drive_only_server.repository.CourseRepository;
import drive_only.drive_only_server.repository.PlaceRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final PlaceRepository placeRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    public CourseService(CourseRepository courseRepository, PlaceRepository placeRepository, CoursePlaceRepository coursePlaceRepository) {
        this.courseRepository = courseRepository;
        this.placeRepository = placeRepository;
        this.coursePlaceRepository = coursePlaceRepository;
    }

    @Transactional
    public CourseCreateResponse createCourse(CourseCreateRequest request) {
        List<CoursePlace> coursePlaces = new ArrayList<>();

        for (CoursePlaceCreateRequest coursePlaceCreateRequest : request.getCoursePlaces()) {
            Place place = placeRepository.findById(Long.parseLong(coursePlaceCreateRequest.getPlaceId()))
                    .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다."));
            CoursePlace coursePlace = new CoursePlace(
                    coursePlaceCreateRequest.getName(),
                    coursePlaceCreateRequest.getPlaceType(),
                    coursePlaceCreateRequest.getContent(),
                    coursePlaceCreateRequest.getSequence(),
                    place);
            coursePlaces.add(coursePlace);
        }

        Member mockMember = new Member("email", "nickname", "url", "provider");

        Course course = new Course(
                request.getTitle(),
                LocalDate.now(),
                request.getRecommendation(), request.getDifficulty(),
                0, 0, 0, false,
                mockMember, //TODO : 나중에 Member 생성되면 LoginMember(현재 로그인 된 사용자)로 변경
                coursePlaces
        );

        for (CoursePlace coursePlace : coursePlaces) {
            coursePlace.setCourse(course);
        }

        Course savedCourse = courseRepository.save(course);
        return new CourseCreateResponse(savedCourse.getId(), "게시글이 성공적으로 등록되었습니다.");
    }
}
