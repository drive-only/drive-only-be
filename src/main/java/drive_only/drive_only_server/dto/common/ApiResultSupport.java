package drive_only.drive_only_server.dto.common;

import drive_only.drive_only_server.success.SuccessCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

public class ApiResultSupport {
    public static <T> ResponseEntity<ApiResult<T>> ok(SuccessCode sc, T result) {
        return ResponseEntity.status(sc.getStatus())
                .body(ApiResult.of(sc.getCode(), sc.getMessage(), result));
    }

    // 성공 + 쿠키 세팅: 재발급 등에서 사용
    public static <T> ResponseEntity<ApiResult<T>> okWithCookies(
            SuccessCode sc,
            T result,
            ResponseCookie... cookies
    ) {
        ApiResult<T> body = ApiResult.of(sc.getCode(), sc.getMessage(), result);
        HttpHeaders headers = new HttpHeaders();
        if (cookies != null) {
            for (ResponseCookie c : cookies) {
                headers.add(HttpHeaders.SET_COOKIE, c.toString());
            }
        }
        return ResponseEntity.status(sc.getStatus()).headers(headers).body(body);
    }
}
