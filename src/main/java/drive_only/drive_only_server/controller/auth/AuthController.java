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

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestBody SocialLoginRequest request) {

        // 카카오 인가코드 → access_token
        // 카카오 access_token → 사용자 정보
        OAuthUserInfo userInfo = oAuth2UserInfoService.kakaoLogin(request.getCode(), request.getRedirectUri());

        // 회원가입/로그인
        Member member = memberService.registerOrLogin(userInfo);

        // 우리 서버 JWT 발급
        String jwt = jwtTokenProvider.createToken(member.getEmail(), member.getProvider());

        return ResponseEntity.ok(new TokenResponse(jwt));
    }

    @PostMapping("/naver")
    public ResponseEntity<TokenResponse> naverLogin(@RequestBody SocialLoginRequest request) {

        // 네이버 인가코드 → access_token
        // 네이버 access_token → 사용자 정보
        OAuthUserInfo userInfo = oAuth2UserInfoService.naverLogin(request.getCode(), request.getRedirectUri());

        // 회원가입/로그인
        Member member = memberService.registerOrLogin(userInfo);

        // 우리 서버 JWT 발급
        String jwt = jwtTokenProvider.createToken(member.getEmail(), member.getProvider());

        return ResponseEntity.ok(new TokenResponse(jwt));
    }
}