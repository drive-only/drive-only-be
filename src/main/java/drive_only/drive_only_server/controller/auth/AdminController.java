package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.auth.AdminLoginRequest;
import drive_only.drive_only_server.dto.auth.AdminLoginResponse;
import drive_only.drive_only_server.dto.common.ApiResult;
import drive_only.drive_only_server.dto.common.ApiResultSupport;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.service.auth.RefreshTokenService;
import drive_only.drive_only_server.repository.member.MemberRepository; // MemberRepository 주입
import drive_only.drive_only_server.success.SuccessCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder 주입
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@Tag(name = "운영자 로그인", description = "운영자 로그인 API")
public class AdminController {

    private final MemberRepository memberRepository; // MemberRepository 사용
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder; // PasswordEncoder 주입

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration; // ms 단위

    @Operation(summary = "운영자 로그인", description = "ID/PW 기반 운영자 로그인 요청")
    @ApiErrorCodeExamples({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_PASSWORD,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/api/admin/login")
    public ResponseEntity<ApiResult<AdminLoginResponse>> adminLogin(@RequestBody AdminLoginRequest request) {

        // 1. ID(이메일)로 회원 조회 (ProviderType.LOCAL 회원만)
        Member admin = memberRepository.findByEmailAndProvider(request.getLoginId(), ProviderType.LOCAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 비밀번호 확인
        if (admin.getPassword() == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. 토큰 생성 (ProviderType.LOCAL 전달)
        String accessToken = jwtTokenProvider.createAccessToken(admin.getEmail(), ProviderType.LOCAL);
        String refreshToken = jwtTokenProvider.createRefreshToken(admin.getEmail(), ProviderType.LOCAL); // 리프레시 토큰에도 Provider 추가

        // 4. refresh token Redis 저장
        refreshTokenService.saveRefreshToken(admin.getEmail(), refreshToken, refreshTokenExpiration);

        // 5. 쿠키 설정 (AuthController 로직 재사용)
        ResponseCookie accessCookie = ResponseCookie.from("access-token", accessToken)
                .httpOnly(true)
                .secure(true)
                .domain("drive-only.com")
                .path("/")
                .maxAge(Duration.ofMillis(jwtTokenProvider.getAccessTokenExpiration()))
                .sameSite("None")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .domain("drive-only.com")
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .sameSite("None")
                .build();

        // 6. 응답 본문 생성 (필요시 정보 추가)
        AdminLoginResponse body = AdminLoginResponse.builder()
                // .nickname(admin.getNickname())
                .build();

        // 7. 응답 반환
        return ApiResultSupport.okWithCookies(
                SuccessCode.SUCCESS_LOGIN, // 적절한 SuccessCode 정의 필요
                body,
                accessCookie,
                refreshCookie
        );
    }
}