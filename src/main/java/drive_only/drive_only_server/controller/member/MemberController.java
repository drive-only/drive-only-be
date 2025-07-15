package drive_only.drive_only_server.controller.member;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.member.MemberResponse;
import drive_only.drive_only_server.service.Member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyProfile(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        String providerString = (String) authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("KAKAO"); // 기본값

        ProviderType provider = ProviderType.valueOf(providerString.toUpperCase());

        Member member = memberService.findByEmailAndProvider(email, provider);

        MemberResponse response = new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getProvider()
        );

        return ResponseEntity.ok(response);
    }
}
