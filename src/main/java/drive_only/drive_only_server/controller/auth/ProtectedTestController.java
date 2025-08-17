package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.dto.common.ApiResult;
import drive_only.drive_only_server.dto.common.ApiResultSupport;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.success.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// AccessToken 유효한지 테스트 하는 컨트롤러
@RestController
@Tag(name = "JWT 토큰", description = "JWT 토큰 유효 확인 API")
public class ProtectedTestController {

    @Operation(
            summary = "JWT 토큰 유효 확인",
            description = "Access Token이 유효하면 200 OK를 반환합니다."
    )
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_TOKEN,          // 서명/형식/만료 등 액세스 토큰 문제
            ErrorCode.UNAUTHENTICATED_MEMBER, // 인증 주체 없음
            ErrorCode.TOKEN_BLACKLISTED,      // (선택) 로그아웃된 토큰 차단을 문서화한 경우
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/api/protected")
    public ResponseEntity<ApiResult<Void>> protectedApi() {
        return ApiResultSupport.ok(SuccessCode.SUCCESS_PROTECTED, null);
    }
}
