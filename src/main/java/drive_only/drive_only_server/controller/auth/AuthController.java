package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.auth.SocialLoginRequest;
import drive_only.drive_only_server.dto.auth.TokenResponse;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.service.Member.MemberService;
import drive_only.drive_only_server.service.oauth.OAuth2UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login")
public class AuthController {

    private final OAuth2UserInfoService oAuth2UserInfoService;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
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