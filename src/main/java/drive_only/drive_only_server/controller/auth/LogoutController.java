package drive_only.drive_only_server.controller.auth;

import drive_only.drive_only_server.security.JwtTokenProvider;
import drive_only.drive_only_server.service.auth.LogoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logout")
public class LogoutController {
    private final LogoutService logoutService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.badRequest().build();
        }

        Date expiration = jwtTokenProvider.getExpirationDate(token);
        long now = System.currentTimeMillis();
        long remainingMillis = expiration.getTime() - now;

        if (remainingMillis > 0) {
            logoutService.blacklistToken(token, remainingMillis);
        }

        return ResponseEntity.ok().build();
    }
}