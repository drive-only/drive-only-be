package drive_only.drive_only_server.controller.photo;

import drive_only.drive_only_server.dto.photo.PhotoUploadRequest;
import drive_only.drive_only_server.dto.photo.PhotoUploadResponse;
import drive_only.drive_only_server.security.CustomUserPrincipal;
import drive_only.drive_only_server.service.photo.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            @RequestBody PhotoUploadRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        String email = userPrincipal.getEmail();
        String imageUrl = photoService.uploadImage(request.getImageData(), email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PhotoUploadResponse("이미지 업로드 성공", imageUrl));
    }
}
