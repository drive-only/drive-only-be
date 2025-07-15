package drive_only.drive_only_server.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//AccessToken 유효한지 테스트 하는 컨트롤러
@RestController
@RequestMapping("/api")
public class ProtectedTestController {

    @GetMapping("/protected")
    public ResponseEntity<String> protectedApi() {
        return ResponseEntity.ok("You have accessed a protected resource!");
    }
}
