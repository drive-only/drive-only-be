package drive_only.drive_only_server.service.oauth;

import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.oauth.KakaoTokenResponse;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserInfoService {

    private final RestTemplate restTemplate;

    // 카카오
    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    // 네이버
    @Value("${naver.client-id}")
    private String naverClientId;

    @Value("${naver.client-secret}")
    private String naverClientSecret;

    @Value("${naver.token-uri}")
    private String naverTokenUri;

    @Value("${naver.user-info-uri}")
    private String naverUserInfoUri;

    // ===================== 카카오 =====================
    public OAuthUserInfo kakaoLogin(String code, String redirectUri) {
        String accessToken = exchangeCodeForAccessToken(
                code, redirectUri,
                kakaoClientId, kakaoClientSecret,
                kakaoTokenUri
        );
        return getKakaoUserInfo(accessToken);
    }

    private OAuthUserInfo getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                kakaoUserInfoUri,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> kakaoAccount = (Map<String, Object>) ((Map)response.getBody().get("kakao_account"));
        String email = (String) kakaoAccount.get("email");
        String nickname = (String) ((Map) kakaoAccount.get("profile")).get("nickname");
        String profileImage = (String) ((Map) kakaoAccount.get("profile")).get("profile_image_url");

        return new OAuthUserInfo(email, nickname, profileImage, ProviderType.KAKAO);
    }

    // ===================== 네이버 =====================
    public OAuthUserInfo naverLogin(String code, String redirectUri) {
        String accessToken = exchangeCodeForAccessToken(
                code, redirectUri,
                naverClientId, naverClientSecret,
                naverTokenUri
        );
        return getNaverUserInfo(accessToken);
    }

    private OAuthUserInfo getNaverUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                naverUserInfoUri,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = (Map) response.getBody().get("response");
        String email = (String) responseBody.get("email");
        String nickname = (String) responseBody.get("nickname");
        String profileImage = (String) responseBody.get("profile_image");

        return new OAuthUserInfo(email, nickname, profileImage, ProviderType.NAVER);
    }

    // ===================== 공통 토큰 교환 =====================
    private String exchangeCodeForAccessToken(
            String code,
            String redirectUri,
            String clientId,
            String clientSecret,
            String tokenUri
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUri,
                request,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }
}