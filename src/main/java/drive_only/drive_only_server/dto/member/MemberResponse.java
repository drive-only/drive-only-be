package drive_only.drive_only_server.dto.member;

import drive_only.drive_only_server.domain.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private ProviderType provider;
}