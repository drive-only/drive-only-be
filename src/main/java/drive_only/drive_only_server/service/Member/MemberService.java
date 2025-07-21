package drive_only.drive_only_server.service.Member;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.oauth.OAuthUserInfo;
import drive_only.drive_only_server.repository.member.MemberRepository;

import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    public Member findByEmailAndProvider(String email, ProviderType provider) {
        return memberRepository.findByEmailAndProvider(email, provider)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Member findByIdAndProvider(Long id, ProviderType provider) {
        return memberRepository.findByIdAndProvider(id, provider)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
    }
}
