package drive_only.drive_only_server.security;

import drive_only.drive_only_server.domain.ProviderType;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.security.JwtTokenProvider.JwtValidationStatus;
import drive_only.drive_only_server.service.auth.LogoutService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final LogoutService logoutService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1) permitAll 경로는 즉시 통과 (SecurityConfig와 동일하게 맞춤)
        boolean isPermitAll =
                path.startsWith("/api/login") ||
                        path.startsWith("/api/auth/") ||
                        path.startsWith("/swagger-ui/") ||
                        path.startsWith("/v3/api-docs/") ||
                        ("GET".equals(request.getMethod()) &&
                                (path.startsWith("/api/courses/") || path.startsWith("/api/places/") || path.equals("/api/categories")));

        if (isPermitAll) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) access-token 추출
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access-token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3) 토큰 없음 → 401 (UNAUTHENTICATED_MEMBER)
        if (token == null || token.isBlank()) {
            request.setAttribute("ERROR_CODE", ErrorCode.UNAUTHENTICATED_MEMBER);
            throw new org.springframework.security.core.AuthenticationException("no token") {};
        }

        // 4) 블랙리스트 → 401
        if (logoutService.isBlacklisted(token)) {
            request.setAttribute("ERROR_CODE", ErrorCode.TOKEN_BLACKLISTED);
            throw new org.springframework.security.core.AuthenticationException("blacklisted") {};
        }

        // 5) 유효성/만료 구분
        JwtValidationStatus status = jwtTokenProvider.getStatus(token);
        if (status == JwtValidationStatus.EXPIRED) {
            request.setAttribute("ERROR_CODE", ErrorCode.TOKEN_EXPIRED);
            throw new org.springframework.security.core.AuthenticationException("expired") {};
        }
        if (status == JwtValidationStatus.INVALID) {
            request.setAttribute("ERROR_CODE", ErrorCode.INVALID_TOKEN);
            throw new org.springframework.security.core.AuthenticationException("invalid") {};
        }

        // 6) Claims 추출 + provider 파싱 실패는 401로
        String email = jwtTokenProvider.getEmail(token);
        String providerStr = jwtTokenProvider.getProvider(token);
        ProviderType provider;
        try {
            provider = ProviderType.valueOf(providerStr.toUpperCase());
        } catch (Exception e) {
            request.setAttribute("ERROR_CODE", ErrorCode.INVALID_TOKEN);
            throw new org.springframework.security.core.AuthenticationException("provider parse fail", e) {};
        }

        // 7) 인증 객체 설정
        CustomUserPrincipal principal = new CustomUserPrincipal(email, provider);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
