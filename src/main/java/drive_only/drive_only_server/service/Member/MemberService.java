package drive_only.drive_only_server.service.Member;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import drive_only.drive_only_server.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member registerOrLogin(OAuthUserInfo userInfo) {
        return memberRepository.findByEmailAndProvider(userInfo.getEmail(), userInfo.getProvider())
                .orElseGet(() -> {
                    Member newMember = Member.createMember(
                            userInfo.getEmail(),
                            userInfo.getNickname(),
                            userInfo.getProfileImageUrl(),
                            userInfo.getProvider()
                    );
                    return memberRepository.save(newMember);
                });
    }
}
