package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;

import drive_only.drive_only_server.security.JwtTokenProvider;
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
            ErrorCode.INVALID_TOKEN,          // 토큰 형식/유효성 실패
            ErrorCode.UNAUTHENTICATED_MEMBER, // 인증 불가 시
            ErrorCode.INTERNAL_SERVER_ERROR
            // 이미 로그아웃된 경우 403을 쓰려면 ErrorCode에 별도 항목 추가(예: ALREADY_LOGGED_OUT)
    })
    @PostMapping("/api/logout")
    public ResponseEntity<ApiResult<Void>> logout(
            @CookieValue(value = "access-token", required = false) String accessToken,
            @CookieValue(value = "refresh-token", required = false) String refreshToken
    ) {
        // 1. access token 유효성 검사 및 블랙리스트 등록
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            Date expiration = jwtTokenProvider.getExpirationDate(accessToken);
            long now = System.currentTimeMillis();
            long remainingMillis = expiration.getTime() - now;

            if (remainingMillis > 0) {
                logoutService.blacklistToken(accessToken, remainingMillis);
            }
        }

        // 2. refresh token 삭제
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String email = jwtTokenProvider.getEmail(refreshToken);
            refreshTokenService.deleteRefreshToken(email);
        }

        // 3. 쿠키 삭제
        ResponseCookie deleteAccessTokenCookie = ResponseCookie.from("access-token", "")
                .httpOnly(true)
                .secure(true)
//                .domain("api.drive-only.com")
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from("refresh-token", "")
                .httpOnly(true)
                .secure(true)
//                .domain("api.drive-only.com")
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        return ApiResultSupport.okWithCookies(
                SuccessCode.SUCCESS_LOGOUT,
                null,
                deleteAccessTokenCookie, deleteRefreshTokenCookie
        );
    }
}
