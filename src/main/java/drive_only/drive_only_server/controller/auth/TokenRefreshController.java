package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.domain.Member;
import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.dto.auth.TokenResponse;
import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.service.Member.MemberService;
import drive_only.drive_only_server.service.auth.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TokenRefreshController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;

    @Operation(
            summary = "Access Token 재발급",
            description = """
        저장된 Refresh Token을 기반으로 새로운 Access Token을 발급합니다.
        
        - 클라이언트는 요청 시 `refresh-token`이라는 이름의 **HttpOnly 쿠키**를 함께 전송해야 합니다.
        - 쿠키에 담긴 Refresh Token이 유효하고 서버에 저장된 토큰과 일치하면 새로운 Access Token을 반환합니다.
        - 만료되었거나 위조된 경우 `401 Unauthorized`를 반환합니다.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 일치하지 않는 Refresh Token", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue(value = "refresh-token", required = false) String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token이 유효하지 않음");
        }

        String email = jwtTokenProvider.getEmail(refreshToken);
        String savedToken = refreshTokenService.getRefreshToken(email);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token 불일치 또는 만료");
        }

        // 💡 provider는 refresh token에서 가져오지 말고 DB에서 가져오자
        Member member = memberService.findByEmail(email);
        ProviderType provider = member.getProvider();

        String newAccessToken = jwtTokenProvider.createAccessToken(email, provider);
        return ResponseEntity.ok().body(new TokenResponse(newAccessToken));
    }
}

