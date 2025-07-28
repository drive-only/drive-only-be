package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.service.auth.LogoutService;
import drive_only.drive_only_server.service.auth.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


@RestController
@RequiredArgsConstructor
@Tag(name = "로그아웃", description = "로그아웃 관련 API")
public class LogoutController {
    private final LogoutService logoutService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "로그아웃", description = "로그아웃 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "소셜 access token 누락 또는 provider 잘못됨", content = @Content),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 JWT access token", content = @Content),
            @ApiResponse(responseCode = "403", description = "이미 로그아웃 되었거나 무효한 사용자", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(value = "refresh-token", required = false) String refreshToken
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.badRequest().build();
        }

        // Access token 블랙리스트 처리
        Date expiration = jwtTokenProvider.getExpirationDate(token);
        long now = System.currentTimeMillis();
        long remainingMillis = expiration.getTime() - now;

        if (remainingMillis > 0) {
            logoutService.blacklistToken(token, remainingMillis);
        }

        // Refresh token Redis에서 삭제
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String email = jwtTokenProvider.getEmail(refreshToken);
            refreshTokenService.deleteRefreshToken(email);
        }

        // 클라이언트 쿠키도 제거
        ResponseCookie deleteCookie = ResponseCookie.from("refresh-token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}