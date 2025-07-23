package drive_only.drive_only_server.security;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.repository.member.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginMemberProvider {
    private final MemberRepository memberRepository;

    public Optional<Member> getLoginMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String email = principal.getEmail();
        ProviderType provider = principal.getProvider();

        return memberRepository.findByEmailAndProvider(email, provider);
    }

    public Optional<Member> getLoginMemberIfExists() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String email = principal.getEmail();
        ProviderType provider = principal.getProvider();

        return memberRepository.findByEmailAndProvider(email, provider);
    }
}
