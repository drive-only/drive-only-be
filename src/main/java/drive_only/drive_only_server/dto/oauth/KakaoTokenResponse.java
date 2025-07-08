package drive_only.drive_only_server.dto.oauth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoTokenResponse {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;
    private String scope;
}