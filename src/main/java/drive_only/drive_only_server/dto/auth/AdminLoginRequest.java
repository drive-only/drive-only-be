package drive_only.drive_only_server.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminLoginRequest {
    private String loginId;
    private String password;
}