package drive_only.drive_only_server.security;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginMemberProvider {
    private final MemberRepository memberRepository;

    public Member getLoginMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        String email = (String) authentication.getPrincipal();
        String providerString = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("KAKAO");
        ProviderType provider = ProviderType.valueOf(providerString.toUpperCase());

        return memberRepository.findByEmailAndProvider(email, provider)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다."));
    }
}
