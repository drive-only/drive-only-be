package drive_only.drive_only_server.controller.member;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.member.MemberResponse;
import drive_only.drive_only_server.dto.member.MemberUpdateRequest;
import drive_only.drive_only_server.dto.member.OtherMemberResponse;
import drive_only.drive_only_server.service.Member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "마이페이지", description = "마이페이지 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "마이페이지 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 쿼리 파라미터", content = @Content),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 JWT access token", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/members/me")
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

    @Operation(summary = "다른 회원 정보 조회", description = "회원 ID로 다른 회원 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다른 회원 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 JWT access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/members/{id}")
    public ResponseEntity<OtherMemberResponse> getOtherMemberProfile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String providerString = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("KAKAO");
        ProviderType provider = ProviderType.valueOf(providerString.toUpperCase());

        Member member = memberService.findByIdAndProvider(id, provider);

        OtherMemberResponse response = new OtherMemberResponse(
                member.getId(),
                member.getNickname(),
                member.getProfileImageUrl()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 닉네임, 프로필 이미지를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 JWT access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PatchMapping("/api/members/me")
    public ResponseEntity<MemberResponse> updateMyProfile(
            @RequestBody MemberUpdateRequest request,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        String providerString = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("KAKAO");
        ProviderType provider = ProviderType.valueOf(providerString.toUpperCase());

        Member updatedMember = memberService.updateMember(email, provider, request);

        MemberResponse response = new MemberResponse(
                updatedMember.getId(),
                updatedMember.getEmail(),
                updatedMember.getNickname(),
                updatedMember.getProfileImageUrl(),
                updatedMember.getProvider()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원 탈퇴")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 JWT access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @DeleteMapping("/api/members/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        String providerString = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("KAKAO");
        ProviderType provider = ProviderType.valueOf(providerString.toUpperCase());

        memberService.deleteMemberByEmailAndProvider(email, provider);

        return ResponseEntity.noContent().build(); // 204 No Content
    }
}