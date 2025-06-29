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
import drive_only.drive_only_server.repository.MemberReporitory;
import drive_only.drive_only_server.repository.PlaceRepository;
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
    private final MemberReporitory memberReporitory;

    @Transactional
    public CourseCreateResponse createCourse(CourseCreateRequest request) {
        List<CoursePlace> coursePlaces = new ArrayList<>();

        for (CoursePlaceCreateRequest coursePlaceCreateRequest : request.getCoursePlaces()) {
            Place place = findPlace(coursePlaceCreateRequest);
            CoursePlace coursePlace = createCoursePlace(coursePlaceCreateRequest, place);
            coursePlaces.add(coursePlace);
        }

        Member testMember = createMember();
        Course course = createCourse(request, testMember, coursePlaces);
        return new CourseCreateResponse(course.getId(), "게시글이 성공적으로 등록되었습니다.");
    }

    private Place findPlace(CoursePlaceCreateRequest coursePlaceCreateRequest) {
        return placeRepository.findById(Long.parseLong(coursePlaceCreateRequest.getPlaceId()))
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다."));
    }

    private CoursePlace createCoursePlace(CoursePlaceCreateRequest coursePlaceCreateRequest, Place place) {
        CoursePlace coursePlace = new CoursePlace(
                coursePlaceCreateRequest.getName(),
                coursePlaceCreateRequest.getPlaceType(),
                coursePlaceCreateRequest.getContent(),
                coursePlaceCreateRequest.getSequence(),
                place);
        coursePlaceRepository.save(coursePlace);
        return coursePlace;
    }

    private Member createMember() {
        Member mockMember = new Member("email", "nickname", "url", "provider");
        memberReporitory.save(mockMember);
        return mockMember;
    }

    private Course createCourse(CourseCreateRequest request, Member testMember, List<CoursePlace> coursePlaces) {
        Course course = Course.createCourse(
                request.getTitle(),
                LocalDate.now(),
                request.getRecommendation(), request.getDifficulty(),
                0, 0, 0, false,
                testMember, //TODO : 나중에 Member 생성되면 LoginMember(현재 로그인 된 사용자)로 변경
                coursePlaces
        );
        return courseRepository.save(course);
    }
}
