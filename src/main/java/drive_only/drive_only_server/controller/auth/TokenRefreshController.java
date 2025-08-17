package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.custom.RefreshTokenNotFoundException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.repository.member.MemberRepository;
import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.service.auth.RefreshTokenService;
import drive_only.drive_only_server.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import drive_only.drive_only_server.dto.common.ApiResult;
import drive_only.drive_only_server.dto.common.ApiResultSupport;
import drive_only.drive_only_server.success.SuccessCode;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@Tag(name = "토큰 재발급", description = "Access Token 재발급 API")
public class TokenRefreshController {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;

    @Operation(
            summary = "Access Token 재발급",
            description = """
        저장된 Refresh Token을 기반으로 새로운 Access Token을 발급합니다.
        
        - 클라이언트는 요청 시 refresh-token이라는 이름의 **HttpOnly 쿠키**를 함께 전송해야 합니다.
        - 쿠키에 담긴 Refresh Token이 유효하고 서버에 저장된 토큰과 일치하면 새로운 Access Token을 반환합니다.
        - 만료되었거나 위조된 경우 401 Unauthorized를 반환합니다.
        """
    )
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_TOKEN,
            ErrorCode.REFRESH_TOKEN_NOT_FOUND,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<ApiResult<Void>> refreshAccessToken(
            @CookieValue(value = "refresh-token", required = false) String refreshToken
    ) {
        // 1) 쿠키 미전달/형식/서명 오류
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 2) 저장된 토큰과 일치 검증
        String email = jwtTokenProvider.getEmail(refreshToken);
        String savedToken = refreshTokenService.getRefreshToken(email);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new RefreshTokenNotFoundException();
        }

        // 3) 회원/프로바이더 로드 (없으면 MemberNotFoundException)
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        ProviderType provider = member.getProvider();

        // 4) 새 access token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(email, provider);

        // 5) Set-Cookie 로 내려줌
        ResponseCookie accessTokenCookie = ResponseCookie.from("access-token", newAccessToken)
                .httpOnly(true)
                .secure(true)
//                .domain("api.drive-only.com")
                .path("/")
                .maxAge(Duration.ofMillis(jwtTokenProvider.getAccessTokenExpiration()))
                .sameSite("None")
                .build();

        // 성공 시에도 JSON(ApiResult) + Set-Cookie 같이 반환
        return ApiResultSupport.okWithCookies(
                SuccessCode.SUCCESS_REFRESH_ACCESS_TOKEN,
                null,                // 최소 변경: result 필요 없으면 null
                accessTokenCookie
        );
    }
}
