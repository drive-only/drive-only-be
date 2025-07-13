package drive_only.drive_only_server.repository.course;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.dto.course.search.CourseSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {
    Page<Course> searchCourses(CourseSearchRequest request, Pageable pageable);
}
