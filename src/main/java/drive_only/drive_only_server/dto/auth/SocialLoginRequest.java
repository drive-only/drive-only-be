package drive_only.drive_only_server.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {
    private String code; // 인가코드
    private String redirectUri; // 클라이언트에서 사용한 redirectUri
}