package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.auth.SocialLoginRequest;
import drive_only.drive_only_server.dto.auth.TokenResponse;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.service.Member.MemberService;
import drive_only.drive_only_server.service.oauth.OAuth2UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "로그인", description = "로그인 관련 API")
public class AuthController {

    private final OAuth2UserInfoService oAuth2UserInfoService;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

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

        Member member = memberService.registerOrLogin(userInfo);
        String jwt = jwtTokenProvider.createToken(member.getEmail(), member.getProvider());
        return ResponseEntity.ok(new TokenResponse(jwt));
    }
}