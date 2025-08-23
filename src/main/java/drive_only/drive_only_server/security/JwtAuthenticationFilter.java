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
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final LogoutService logoutService;

    private static final PathPatternParser PPP = new PathPatternParser();

    private static final PathPattern LOGIN     = PPP.parse("/api/login");
    private static final PathPattern AUTH_ALL  = PPP.parse("/api/auth/**");
    private static final PathPattern SWAGGER   = PPP.parse("/swagger-ui/**");
    private static final PathPattern DOCS      = PPP.parse("/v3/api-docs/**");
    private static final PathPattern COURSES   = PPP.parse("/api/courses/**");
    private static final PathPattern PLACES    = PPP.parse("/api/places/**");
    private static final PathPattern CATEGORIES= PPP.parse("/api/categories");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        log.info("JWT-FILTER path='{}', servletPath='{}', method='{}', query='{}'",
                request.getRequestURI(), request.getServletPath(), request.getMethod(), request.getQueryString());

        final String method = request.getMethod();
        final String path = request.getServletPath();

        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        PathContainer pc = PathContainer.parsePath(path);

        if (LOGIN.matches(pc)) return true;
        if (AUTH_ALL.matches(pc)) return true;
        if (SWAGGER.matches(pc)) return true;
        if (DOCS.matches(pc)) return true;

        if (HttpMethod.GET.matches(method)) {
            if (COURSES.matches(pc)) return true;
            if (PLACES.matches(pc)) return true;
            if (CATEGORIES.matches(pc)) return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null || token.isBlank()) {
            SecurityContextHolder.clearContext();
            chain.doFilter(request, response);
            return;
        }

        if (logoutService.isBlacklisted(token)) {
            request.setAttribute("ERROR_CODE", ErrorCode.TOKEN_BLACKLISTED);
            throw new org.springframework.security.core.AuthenticationException("blacklisted") {};
        }

        JwtValidationStatus status = jwtTokenProvider.getStatus(token);
        if (status == JwtValidationStatus.VALID) {
            setAuthentication(token, request);
            chain.doFilter(request, response);
            return;
        } else {
            request.setAttribute("ERROR_CODE",
                    status == JwtValidationStatus.EXPIRED ? ErrorCode.TOKEN_EXPIRED : ErrorCode.INVALID_TOKEN);
            throw new org.springframework.security.core.AuthenticationException("invalid/expired") {};
        }
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

    private void setAuthentication(String token, HttpServletRequest request) {
        final String email = jwtTokenProvider.getEmail(token);
        final String providerStr = jwtTokenProvider.getProvider(token);

        final ProviderType provider;
        try {
            provider = ProviderType.valueOf(String.valueOf(providerStr).toUpperCase());
        } catch (RuntimeException e) {
            request.setAttribute("ERROR_CODE", ErrorCode.INVALID_TOKEN);
            throw new org.springframework.security.core.AuthenticationException("provider parse fail", e) {};
        }

        var principal = new CustomUserPrincipal(email, provider);
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
