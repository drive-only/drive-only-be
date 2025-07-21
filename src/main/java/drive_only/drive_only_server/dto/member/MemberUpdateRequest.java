package drive_only.drive_only_server.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateRequest {
    private String nickname;
    private String profileImageUrl;
}