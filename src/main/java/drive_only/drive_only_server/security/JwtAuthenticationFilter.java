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
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String path = request.getRequestURI();
        final String method = request.getMethod();

        boolean isPermitAll =
                "OPTIONS".equalsIgnoreCase(method) ||
                        path.startsWith("/api/login") ||
                        path.startsWith("/api/auth/") ||
                        path.startsWith("/swagger-ui/") ||
                        path.startsWith("/v3/api-docs/") ||
                        ("GET".equals(method) && (
                                path.equals("/api/courses") || path.startsWith("/api/courses/") ||
                                        path.equals("/api/places")  || path.startsWith("/api/places/")  ||
                                        path.equals("/api/categories") || path.startsWith("/api/categories/")
                        ));

        String token = resolveToken(request);

        if (token != null && !token.isBlank()) {
            if (logoutService.isBlacklisted(token)) {
                if (!isPermitAll) {
                    request.setAttribute("ERROR_CODE", ErrorCode.ACCESS_TOKEN_BLACKLISTED);
                    restAuthenticationEntryPoint.commence(
                            request, response,
                            new org.springframework.security.authentication.InsufficientAuthenticationException("blacklisted")
                    );
                    return;
                }
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            JwtValidationStatus status = jwtTokenProvider.getStatus(token);
            if (status == JwtValidationStatus.VALID) {
                setAuthentication(token, request);
                chain.doFilter(request, response);
                return;
            } else {
                if (!isPermitAll) {
                    if (status == JwtValidationStatus.EXPIRED) {
                        request.setAttribute("ERROR_CODE", ErrorCode.ACCESS_TOKEN_EXPIRED);
                    } else {
                        request.setAttribute("ERROR_CODE", ErrorCode.ACCESS_TOKEN_INVALID);
                    }
                    restAuthenticationEntryPoint.commence(
                            request, response,
                            new org.springframework.security.authentication.InsufficientAuthenticationException("invalid/expired")
                    );
                    return;
                }
                SecurityContextHolder.clearContext();
            }
        } else {
            if (!isPermitAll) {
                request.setAttribute("ERROR_CODE", ErrorCode.ACCESS_TOKEN_EMPTY_ERROR);
                restAuthenticationEntryPoint.commence(
                        request, response,
                        new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("missing token")
                );
                return; // [ADDED]
            }
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access-token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setAuthentication(String token, HttpServletRequest request) throws IOException {
        final String email = jwtTokenProvider.getEmail(token);
        final String providerStr = jwtTokenProvider.getProvider(token);

        final ProviderType provider;
        try {
            provider = ProviderType.valueOf(String.valueOf(providerStr).toUpperCase());
        } catch (RuntimeException e) {
            request.setAttribute("ERROR_CODE", ErrorCode.ACCESS_TOKEN_INVALID);
            restAuthenticationEntryPoint.commence(
                    request, (HttpServletResponse) request.getAttribute("HTTP_RESPONSE"),
                    new org.springframework.security.authentication.InsufficientAuthenticationException("provider parse fail", e)
            );
            return;
        }

        var principal = new CustomUserPrincipal(email, provider);
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
