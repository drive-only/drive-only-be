package drive_only.drive_only_server.controller.member;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.course.list.MyCourseListResponse;
import drive_only.drive_only_server.dto.likedCourse.list.LikedCourseListResponse;
import drive_only.drive_only_server.dto.member.MemberResponse;
import drive_only.drive_only_server.dto.member.MemberUpdateRequest;
import drive_only.drive_only_server.dto.member.OtherMemberResponse;
import drive_only.drive_only_server.security.CustomUserPrincipal;
import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.security.LoginMemberProvider;
import drive_only.drive_only_server.service.auth.RefreshTokenService;
import drive_only.drive_only_server.service.member.MemberService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;
    private final LoginMemberProvider loginMemberProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "마이페이지", description = "마이페이지 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "마이페이지 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 쿼리 파라미터", content = @Content),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 JWT access token", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/members/me")
    public ResponseEntity<MemberResponse> getMyProfile() {
        Member member = loginMemberProvider.getLoginMember();

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
    public ResponseEntity<OtherMemberResponse> getOtherMemberProfile(@PathVariable Long id) {
        Member member = memberService.findById(id);

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
            @RequestBody MemberUpdateRequest request
    ) {
        Member loginMember = loginMemberProvider.getLoginMember();

        Member updatedMember = memberService.updateMember(
                loginMember.getEmail(),
                loginMember.getProvider(),
                request
        );

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
    public ResponseEntity<Void> deleteMyAccount(
            Authentication authentication,
            @CookieValue(value = "refresh-token", required = false) String refreshToken
    ) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String email = principal.getEmail();
        ProviderType provider = principal.getProvider();

        // 1. 회원 삭제
        memberService.deleteMemberByEmailAndProvider(email, provider);

        // 2. refresh token 삭제
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            refreshTokenService.deleteRefreshToken(email);
        }

        // 3. access-token 및 refresh-token 쿠키 삭제 설정
        ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from("refresh-token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        ResponseCookie deleteAccessTokenCookie = ResponseCookie.from("access-token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        // 4. 다중 쿠키 설정을 위한 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, deleteAccessTokenCookie.toString());

        // 5. 응답 반환
        return ResponseEntity.noContent()
                .headers(headers)
                .build();
    }

    @Operation(summary = "좋아요한 코스 조회", description = "회원이 좋아요한 드라이브 코스를 최신순으로 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 인증", content = @Content),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/members/me/likedCourses")
    public ResponseEntity<LikedCourseListResponse> getLikedCourses(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size
    ) {
        Member member = loginMemberProvider.getLoginMember();
        LikedCourseListResponse response = memberService.getLikedCourses(member, lastId, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 작성한 코스 조회", description = "인증된 사용자의 작성 코스를 커서 기반으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성한 코스 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/members/me/courses")
    public ResponseEntity<MyCourseListResponse> getMyCourses(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size
    ) {
        Member member = loginMemberProvider.getLoginMember();
        MyCourseListResponse response = memberService.getMyCourses(member, lastId, size);
        return ResponseEntity.ok(response);
    }
}