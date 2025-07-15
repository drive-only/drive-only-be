package drive_only.drive_only_server.security;

import drive_only.drive_only_server.domain.ProviderType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.security.SignatureException;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION;

    //토큰 생성
    public String createToken(String email, ProviderType provider) {
        return Jwts.builder()
                .setSubject(email)
                .claim("provider", provider.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    //토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (SignatureException | ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    //토큰에서 email 추출
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    //토큰에서 provider 추출 (Optional)
    public String getProvider(String token) {
        return (String) getClaims(token).get("provider");
    }

    //공통 claims 파싱 메서드
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    //만료일 추출 메서드
    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }
}