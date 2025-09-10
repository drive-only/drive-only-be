package drive_only.drive_only_server.repository.coursePlace;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.CoursePlace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {
    @Query("""
        select cp
        from CoursePlace cp
        join fetch cp.place
        where cp.course = :course
        order by cp.sequence
    """)
    List<CoursePlace> findByCourse(Course course);
}
