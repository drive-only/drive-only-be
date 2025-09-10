package drive_only.drive_only_server.repository.course;

import drive_only.drive_only_server.domain.LikedCourse;
import drive_only.drive_only_server.domain.Member;

import java.util.List;

public interface LikedCourseRepositoryCustom {
    List<LikedCourse> findLikedCoursesByMember(Member member, Long lastId, int size);
}

