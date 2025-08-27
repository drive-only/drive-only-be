package drive_only.drive_only_server.repository.course;

import drive_only.drive_only_server.domain.Course;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {
    @Query("""
        select c
        from Course c
        join fetch c.member
        where c.id = :courseId
    """)
    Optional<Course> findWithMemberById(Long courseId);
}
