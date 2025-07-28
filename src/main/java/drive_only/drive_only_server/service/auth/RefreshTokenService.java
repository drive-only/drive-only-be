package drive_only.drive_only_server.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "refresh:";

    // Redis에 저장
    public void saveRefreshToken(String email, String token, long durationMillis) {
        redisTemplate.opsForValue().set(PREFIX + email, token, durationMillis, TimeUnit.MILLISECONDS);
    }

    // Redis에서 가져오기
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(PREFIX + email);
    }

    // Redis에서 삭제
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(PREFIX + email);
    }
}
