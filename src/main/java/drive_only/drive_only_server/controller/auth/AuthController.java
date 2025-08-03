package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.auth.SocialLoginRequest;
import drive_only.drive_only_server.dto.auth.TokenResponse;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.service.Member.MemberService;
import drive_only.drive_only_server.service.auth.RefreshTokenService;
import drive_only.drive_only_server.service.oauth.OAuth2UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 JWT access token", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/api/login")
    public ResponseEntity<TokenResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        OAuthUserInfo userInfo;

        if ("KAKAO".equalsIgnoreCase(request.getProvider())) {
            userInfo = oAuth2UserInfoService.kakaoLogin(request.getCode());
        } else if ("NAVER".equalsIgnoreCase(request.getProvider())) {
            userInfo = oAuth2UserInfoService.naverLogin(request.getCode());
        } else {
            return ResponseEntity.badRequest().build();
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
                .path("/")
                .maxAge(Duration.ofMillis(jwtTokenProvider.getAccessTokenExpiration()))
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .sameSite("Strict")
                .build();

        // 5. 응답
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
                // .body(new TokenResponse(accessToken));  accessToken body 포함 여부는 선택
    }
}
