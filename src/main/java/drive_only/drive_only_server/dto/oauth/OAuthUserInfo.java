package drive_only.drive_only_server.dto.oauth;

import drive_only.drive_only_server.domain.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUserInfo {
    private String email;
    private String nickname;
    private String profileImageUrl;
    private ProviderType provider;
}
