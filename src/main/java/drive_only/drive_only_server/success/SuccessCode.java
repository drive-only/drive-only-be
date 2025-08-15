package drive_only.drive_only_server.success;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {
    // Course
    SUCCESS_GET_COURSES(HttpStatus.OK, "게시글 목록을 불러오는 데 성공했습니다."),
    SUCCESS_GET_COURSE_DETAIL(HttpStatus.OK, "게시글 상세를 불러오는 데 성공했습니다."),
    SUCCESS_CREATE_COURSE(HttpStatus.CREATED, "게시글이 성공적으로 등록되었습니다."),
    SUCCESS_UPDATE_COURSE(HttpStatus.OK, "게시글이 성공적으로 수정되었습니다."),
    SUCCESS_DELETE_COURSE(HttpStatus.OK, "게시글이 성공적으로 삭제되었습니다."),
    SUCCESS_TOGGLE_COURSE_LIKE(HttpStatus.OK, "게시글 좋아요 상태가 변경되었습니다."),

    // Comment
    SUCCESS_GET_COMMENTS(HttpStatus.OK, "댓글 목록을 불러오는 데 성공했습니다."),
    SUCCESS_CREATE_COMMENT(HttpStatus.CREATED, "댓글이 성공적으로 등록되었습니다."),
    SUCCESS_UPDATE_COMMENT(HttpStatus.OK, "댓글이 성공적으로 수정되었습니다."),
    SUCCESS_DELETE_COMMENT(HttpStatus.OK, "댓글이 성공적으로 삭제되었습니다."),
    SUCCESS_TOGGLE_COMMENT_LIKE(HttpStatus.OK, "댓글 좋아요 상태가 변경되었습니다."),

    // Place
    SUCCESS_GET_PLACES(HttpStatus.OK, "장소 목록을 불러오는 데 성공했습니다."),
    SUCCESS_GET_NEARBY_PLACES(HttpStatus.OK, "주변 장소 목록을 불러오는 데 성공했습니다."),
    SUCCESS_GET_SAVED_PLACES(HttpStatus.OK, "저장한 장소 목록을 불러오는 데 성공했습니다."),
    SUCCESS_SAVE_PLACE(HttpStatus.CREATED, "장소가 성공적으로 저장되었습니다."),
    SUCCESS_DELETE_SAVED_PLACE(HttpStatus.OK, "저장한 장소가 성공적으로 삭제되었습니다.");

    private final HttpStatus status;
    private final String message;

    public String getCode() {
        return this.name();
    }
}
