package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;

import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.security.JwtTokenProvider.JwtValidationStatus;
import drive_only.drive_only_server.service.auth.LogoutService;
import drive_only.drive_only_server.service.auth.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import drive_only.drive_only_server.dto.common.ApiResult;
import drive_only.drive_only_server.dto.common.ApiResultSupport;
import drive_only.drive_only_server.success.SuccessCode;

import java.util.Date;


@RestController
@RequiredArgsConstructor
@Tag(name = "로그아웃", description = "로그아웃 관련 API")
public class LogoutController {
    private final LogoutService logoutService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "로그아웃", description = "로그아웃 요청")
    @ApiErrorCodeExamples({
            // 401 Access
            ErrorCode.ACCESS_TOKEN_EMPTY_ERROR,
            ErrorCode.ACCESS_TOKEN_EXPIRED,
            ErrorCode.ACCESS_TOKEN_INVALID,
            ErrorCode.ACCESS_TOKEN_BLACKLISTED,

            // 500
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/logout")
    public ResponseEntity<ApiResult<Void>> logout(
            @CookieValue(value = "access-token", required = false) String accessToken,
            @CookieValue(value = "refresh-token", required = false) String refreshToken
    ) {
        // 1) 액세스 토큰은 유효하면 블랙리스트, 만료면 스킵(선택)
        if (accessToken != null) {
            JwtValidationStatus st = jwtTokenProvider.getStatus(accessToken);
            if (st == JwtValidationStatus.VALID) {
                long remain = jwtTokenProvider.getExpirationDate(accessToken).getTime() - System.currentTimeMillis();
                if (remain > 0) logoutService.blacklistToken(accessToken, remain);
            }
        }

        // 2) 리프레시 토큰은 만료여도 이메일만 뽑아서 서버 저장소에서 제거 시도
        if (refreshToken != null) {
            String email = jwtTokenProvider.getEmailAllowExpired(refreshToken);
            if (email != null) refreshTokenService.deleteRefreshToken(email);
        }

        // 3) 쿠키 삭제(로그인 때와 '속성 100% 동일'하게)
        ResponseCookie clearAccess = ResponseCookie.from("access-token", "")
                .httpOnly(true).secure(true).domain("drive-only.com").path("/")
                .sameSite("None").maxAge(0).build();

        ResponseCookie clearRefresh = ResponseCookie.from("refresh-token", "")
                .httpOnly(true).secure(true).domain("drive-only.com").path("/")
                .sameSite("None").maxAge(0).build();

        return ApiResultSupport.okWithCookies(SuccessCode.SUCCESS_LOGOUT, null, clearAccess, clearRefresh);
    }
}
