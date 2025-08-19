package drive_only.drive_only_server.repository.hide;

import drive_only.drive_only_server.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface HiddenCourseRepository extends JpaRepository<HiddenCourse, Long> {
    boolean existsByCourseAndMember(Course course, Member member);
    void deleteByCourseAndMember(Course course, Member member);
}
