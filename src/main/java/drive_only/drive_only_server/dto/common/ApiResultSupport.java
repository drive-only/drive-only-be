package drive_only.drive_only_server.dto.common;

import drive_only.drive_only_server.success.SuccessCode;
import org.springframework.http.ResponseEntity;

public class ApiResultSupport {
    public static <T> ResponseEntity<ApiResult<T>> ok(SuccessCode sc, T result) {
        return ResponseEntity.status(sc.getStatus())
                .body(ApiResult.of(sc.getCode(), sc.getMessage(), result));
    }
}
