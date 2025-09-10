package drive_only.drive_only_server.service.oauth;

import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import drive_only.drive_only_server.exception.custom.InvalidOAuthCodeException;
import drive_only.drive_only_server.exception.custom.OAuthCommunicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserInfoService {

    private final RestTemplate restTemplate;

    // 카카오
    @Value("${kakao.client-id}")     private String kakaoClientId;
    @Value("${kakao.client-secret}") private String kakaoClientSecret;
    @Value("${kakao.token-uri}")     private String kakaoTokenUri;
    @Value("${kakao.user-info-uri}") private String kakaoUserInfoUri;

    // 네이버
    @Value("${naver.client-id}")     private String naverClientId;
    @Value("${naver.client-secret}") private String naverClientSecret;
    @Value("${naver.token-uri}")     private String naverTokenUri;
    @Value("${naver.user-info-uri}") private String naverUserInfoUri;

    // ===================== 카카오 =====================
    public OAuthUserInfo kakaoLogin(String code) {
        String accessToken = exchangeCodeForAccessToken(code, kakaoClientId, kakaoClientSecret, kakaoTokenUri);
        return getKakaoUserInfo(accessToken);
    }

    private OAuthUserInfo getKakaoUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    kakaoUserInfoUri, HttpMethod.GET, entity, Map.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new OAuthCommunicationException("Kakao userinfo response invalid");
            }

            Map body = response.getBody();
            Map account = (Map) body.get("kakao_account");
            if (account == null) {
                throw new OAuthCommunicationException("Kakao account is null");
            }
            String email = (String) account.get("email");
            Map profile = (Map) account.get("profile");
            String nickname = profile == null ? null : (String) profile.get("nickname");
            String profileImage = profile == null ? null : (String) profile.get("profile_image_url");

            if (email == null || email.isBlank()) {
                throw new OAuthCommunicationException("Kakao email not provided or empty");
            }

            return new OAuthUserInfo(email, nickname, profileImage, ProviderType.KAKAO);
        } catch (RestClientException e) {
            throw new OAuthCommunicationException("Kakao userinfo call failed", e);
        }
    }

    // ===================== 네이버 =====================
    public OAuthUserInfo naverLogin(String code) {
        String accessToken = exchangeCodeForAccessToken(code, naverClientId, naverClientSecret, naverTokenUri);
        return getNaverUserInfo(accessToken);
    }

    private OAuthUserInfo getNaverUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    naverUserInfoUri, HttpMethod.GET, entity, Map.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new OAuthCommunicationException("Naver userinfo response invalid");
            }

            Map body = response.getBody();
            Map responseBody = (Map) body.get("response");
            if (responseBody == null) {
                throw new OAuthCommunicationException("Naver response body is null");
            }
            String email = (String) responseBody.get("email");
            String nickname = (String) responseBody.get("nickname");
            String profileImage = (String) responseBody.get("profile_image");

            if (email == null || email.isBlank()) {
                throw new OAuthCommunicationException("Naver email not provided or empty");
            }

            return new OAuthUserInfo(email, nickname, profileImage, ProviderType.NAVER);
        } catch (RestClientException e) {
            throw new OAuthCommunicationException("Naver userinfo call failed", e);
        }
    }

    // ===================== 공통 토큰 교환 =====================
    private String exchangeCodeForAccessToken(String code, String clientId, String clientSecret, String tokenUri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new InvalidOAuthCodeException();
            }

            String accessToken = (String) response.getBody().get("access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new InvalidOAuthCodeException();
            }

            return accessToken;
        } catch (RestClientException e) {
            // 통신 자체 실패는 통신 예외, 400류 응답/토큰 누락은 인가 코드 예외
            throw new OAuthCommunicationException("Token exchange call failed", e);
        }
    }
}
