package drive_only.drive_only_server.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtherMemberResponse {
    private Long id;
    private String nickname;
    private String profileImageUrl;
}