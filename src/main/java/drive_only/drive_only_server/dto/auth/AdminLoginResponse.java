package drive_only.drive_only_server.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminLoginResponse {
    private String nickname;
}