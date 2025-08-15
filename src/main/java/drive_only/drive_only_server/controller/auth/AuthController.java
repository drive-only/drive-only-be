package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.auth.SocialLoginRequest;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.service.member.MemberService;
import drive_only.drive_only_server.service.auth.RefreshTokenService;
import drive_only.drive_only_server.service.oauth.OAuth2UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@Tag(name = "로그인", description = "로그인 관련 API")
public class AuthController {

    private final OAuth2UserInfoService oAuth2UserInfoService;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration; // ms 단위

    @Operation(summary = "로그인", description = "로그인 요청")
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_PROVIDER,
            ErrorCode.INVALID_OAUTH_CODE,
            ErrorCode.OAUTH_COMMUNICATION_FAILED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/login")
    public ResponseEntity<Void> socialLogin(@RequestBody SocialLoginRequest request) {
        OAuthUserInfo userInfo;

        if ("KAKAO".equalsIgnoreCase(request.getProvider())) {
            userInfo = oAuth2UserInfoService.kakaoLogin(request.getCode()); // 내부에서 예외 표준화
        } else if ("NAVER".equalsIgnoreCase(request.getProvider())) {
            userInfo = oAuth2UserInfoService.naverLogin(request.getCode()); // 내부에서 예외 표준화
        } else {
            // 일관된 에러 포맷
            throw new BusinessException(ErrorCode.INVALID_PROVIDER);
        }

        // 1. 회원 등록 or 로그인
        Member member = memberService.registerOrLogin(userInfo);

        // 2. access & refresh token 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getProvider());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        // 3. refresh token Redis 저장
        refreshTokenService.saveRefreshToken(member.getEmail(), refreshToken, refreshTokenExpiration);

        // 4. 쿠키 설정 (access + refresh)
        ResponseCookie accessCookie = ResponseCookie.from("access-token", accessToken)
                .httpOnly(true)
                .secure(true)
//                .domain("api.drive-only.com")
                .path("/")
                .maxAge(Duration.ofMillis(jwtTokenProvider.getAccessTokenExpiration()))
                .sameSite("None")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(true)
//                .domain("api.drive-only.com")
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .sameSite("None")
                .build();

        // 5. 응답
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }
}
