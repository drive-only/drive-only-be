package drive_only.drive_only_server.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //400 BAD REQUEST
    INVALID_COURSE_TITLE(HttpStatus.BAD_REQUEST, "제목은 필수이며 70자 이하로 입력해야 합니다."),
    INVALID_COURSE_RECOMMENDATION(HttpStatus.BAD_REQUEST, "추천도는 0 이상 5 이하의 값이어야 합니다."),
    INVALID_COURSE_DIFFICULTY(HttpStatus.BAD_REQUEST, "난이도는 0 이상 5 이하의 값이어야 합니다."),
    COURSE_PLACES_REQUIRED(HttpStatus.BAD_REQUEST, "코스 장소는 1개 이상이어야 합니다."),

    //404 NOT FOUND
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 코스(게시글)를 찾을 수 없습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 장소를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),

    UNAUTHORIZED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    //500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    public String getCode() {
        return this.name();
    }
}
