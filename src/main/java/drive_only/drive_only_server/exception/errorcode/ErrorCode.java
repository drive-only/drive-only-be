package drive_only.drive_only_server.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //400 BAD REQUEST
    INVALID_COURSE_TITLE(HttpStatus.BAD_REQUEST, "제목은 1자 이상 70자 이하로 입력해주세요."),
    INVALID_COURSE_RECOMMENDATION(HttpStatus.BAD_REQUEST, "추천도는 0부터 5까지의 값이어야 합니다."),
    INVALID_COURSE_DIFFICULTY(HttpStatus.BAD_REQUEST, "난이도는 0부터 5까지의 값이어야 합니다."),
    INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "댓글은 1자 이상 200자 이하로 입력해주세요."),
    INVALID_COURSE_PLACE_CONTENT(HttpStatus.BAD_REQUEST, "내용은 0자 이상 500자 이하로 입력해주세요."),
    INVALID_COURSE_PLACE_PHOTOS(HttpStatus.BAD_REQUEST, "사진 첨부는 최대 5개까지 가능합니다."),
    COURSE_PLACES_REQUIRED(HttpStatus.BAD_REQUEST, "코스 장소는 최소 1개 이상 등록되어야 합니다."),
    KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요."),
    KEYWORD_WITH_CATEGORY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "검색어와 카테고리 필터는 동시에 사용할 수 없습니다."),

    //404 NOT FOUND
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 코스(게시글)를 찾을 수 없습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 장소를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."),

    UNAUTHORIZED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    OWNER_MISMATCH(HttpStatus.FORBIDDEN, "작성자만 수정 또는 삭제할 수 있습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    UNAUTHENTICATED_MEMBER(HttpStatus.UNAUTHORIZED, "로그인이 필요한 요청입니다."),

    //500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    public String getCode() {
        return this.name();
    }
}
