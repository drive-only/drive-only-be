package drive_only.drive_only_server.repository.course;

import drive_only.drive_only_server.domain.Course;
import drive_only.drive_only_server.domain.LikedCourse;
import drive_only.drive_only_server.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikedCourseRepository extends JpaRepository<LikedCourse, Long>, LikedCourseRepositoryCustom {
    Optional<LikedCourse> findByCourseAndMember(Course course, Member member);
}