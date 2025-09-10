package drive_only.drive_only_server.repository.course;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.dto.course.search.CourseSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CourseRepositoryCustom {
    List<Course> findCoursesByMember(Long memberId, Long lastId, int size);

    Page<Course> searchCourses(CourseSearchRequest request, Pageable pageable, Long viewerId);
}
