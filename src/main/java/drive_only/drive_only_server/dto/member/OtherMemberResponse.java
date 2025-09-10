package drive_only.drive_only_server.dto.member;

import drive_only.drive_only_server.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtherMemberResponse {
    private Long id;
    private String nickname;
    private String profileImageUrl;

    public static OtherMemberResponse from(Member member) {
        return new OtherMemberResponse(
                member.getId(),
                member.getNickname(),
                member.getProfileImageUrl()
        );
    }
}