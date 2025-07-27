package drive_only.drive_only_server.repository.comment;

import drive_only.drive_only_server.domain.Comment;
import drive_only.drive_only_server.domain.LikedComment;
import drive_only.drive_only_server.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikedCommentRepository extends JpaRepository<LikedComment, Long> {
    boolean existsByCommentAndMember(Comment comment, Member member);
    void deleteByCommentAndMember(Comment comment, Member member);
    int countByComment(Comment comment);
    Optional<LikedComment> findByCommentAndMember(Comment comment, Member member);
}