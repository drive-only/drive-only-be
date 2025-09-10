package drive_only.drive_only_server.dto.auth;

import drive_only.drive_only_server.domain.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String profileImageUrl;
}
