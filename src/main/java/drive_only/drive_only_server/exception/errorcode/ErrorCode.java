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
    INVALID_CATEGORY_REGION(HttpStatus.BAD_REQUEST, "지역은 필수 입력 항목입니다."),
    INVALID_CATEGORY_SEASON(HttpStatus.BAD_REQUEST, "계절은 필수 입력 항목입니다."),
    INVALID_CATEGORY_AREA_TYPE(HttpStatus.BAD_REQUEST, "근교/외곽 구분은 필수 입력 항목입니다."),
    COURSE_PLACES_REQUIRED(HttpStatus.BAD_REQUEST, "코스 장소는 최소 1개 이상 등록되어야 합니다."),
    KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요."),
    PLACE_ID_WITH_ANYTHING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "장소ID와 검색어 및 카테고리 필터는 동시에 적용될 수 없습니다."),
    KEYWORD_WITH_CATEGORY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "검색어와 카테고리 필터는 동시에 적용될 수 없습니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "유효한 이메일 형식이 아닙니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 2~20자, 영문/숫자/한글/._- 만 가능합니다."),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "유효하지 않은 소셜 제공자입니다."),
    INVALID_IMAGE_DATA(HttpStatus.BAD_REQUEST, "잘못된 이미지 데이터입니다."),
    INVALID_PHOTO_MAPPING(HttpStatus.BAD_REQUEST, "photo 매핑이 유요하지 않습니다."),
    INVALID_OAUTH_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인가 코드입니다."),

    //401 UNAUTHORIZED
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "이미 가입된 회원입니다."), // (선택) 동시가입 경합 구분용
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다."), // (선택) 구분 필요 시,
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    UNAUTHENTICATED_MEMBER(HttpStatus.UNAUTHORIZED, "로그인이 필요한 요청입니다."),

    // Access 관련
    ACCESS_TOKEN_EMPTY_ERROR(HttpStatus.UNAUTHORIZED, "액세스 토큰이 비어 있습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    ACCESS_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    ACCESS_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "로그아웃된 액세스 토큰입니다."),

    // Refresh 관련
    REFRESH_TOKEN_EMPTY_ERROR(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 비어 있습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "저장된 리프레시 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다."),

    //403 FORBIDDEN
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    OWNER_MISMATCH(HttpStatus.FORBIDDEN, "작성자만 수정 또는 삭제할 수 있습니다."),

    //404 NOT FOUND
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 코스(게시글)를 찾을 수 없습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 장소를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."),
    SAVED_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "저장되지 않은 장소입니다."),

    //500 INTERNAL SERVER ERROR,
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다."),
    FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),

    //502 BAD_GATEWAY
    OAUTH_COMMUNICATION_FAILED(HttpStatus.BAD_GATEWAY, "OAuth 서버 통신에 실패했습니다."),
    TOUR_API_COMMUNICATION_FAILED(HttpStatus.BAD_GATEWAY, "관광공사 API 통신에 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    public String getCode() {
        return this.name();
    }
}
