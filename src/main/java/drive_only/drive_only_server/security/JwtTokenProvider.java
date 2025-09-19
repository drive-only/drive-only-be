package drive_only.drive_only_server.security;

import drive_only.drive_only_server.domain.ProviderType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}") // access token 만료시간 (ms)
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-expiration}") // refresh token 만료시간 (ms)
    private long REFRESH_TOKEN_EXPIRATION;

    // Access Token 생성
    public String createAccessToken(String email, ProviderType provider) {
        return Jwts.builder()
                .setSubject(email)
                .claim("provider", provider.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String email, ProviderType provider) {
        return Jwts.builder()
                .setSubject(email) // 사용자 식별
                .claim("provider", provider.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 공통 Claims 파싱
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (SignatureException | ExpiredJwtException | MalformedJwtException e) {
            return false;
        }
    }

    public enum JwtValidationStatus { VALID, EXPIRED, INVALID }

    // 상태 구분 메서드
    public JwtValidationStatus getStatus(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return JwtValidationStatus.VALID;
        } catch (ExpiredJwtException e) {
            return JwtValidationStatus.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            return JwtValidationStatus.INVALID;
        }
    }

    // 토큰에서 이메일 추출
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    // 토큰에서 provider 추출 (Access token 전용)
    public String getProvider(String token) {
        return (String) getClaims(token).get("provider");
    }

    // 만료일 추출
    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    public long getAccessTokenExpiration() {
        return ACCESS_TOKEN_EXPIRATION;
    }

    public String getEmailAllowExpired(String token) {
        try {
            return getEmail(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject(); // 만료여도 subject 추출
        }
    }
}
