package drive_only.drive_only_server.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//AccessToken 유효한지 테스트 하는 컨트롤러
@RestController
@Tag(name = "JWT 토큰", description = "JWT 토큰 유효 확인 API")
public class ProtectedTestController {

    @Operation(summary = "JWT 토큰", description = "JWT 토큰 유효 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT 토큰 유효"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 JWT access token", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/api/protected")
    public ResponseEntity<String> protectedApi() {
        return ResponseEntity.ok("You have accessed a protected resource!");
    }
}
