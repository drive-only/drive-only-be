package drive_only.drive_only_server.controller.photo;

import drive_only.drive_only_server.dto.photo.PhotoUploadRequest;
import drive_only.drive_only_server.dto.photo.PhotoUploadResponse;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.security.CustomUserPrincipal;
import drive_only.drive_only_server.service.photo.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photos")
@Tag(name = "사진", description = "사진 업로드 API")
public class PhotoController {

    private final PhotoService photoService;

    @Operation(summary = "사진 업로드", description = "Base64 data URL 이미지를 업로드하고 S3 URL을 반환합니다.")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.INVALID_IMAGE_DATA,
            ErrorCode.FILE_UPLOAD_FAIL,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            @RequestBody PhotoUploadRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_MEMBER);
        }

        String email = userPrincipal.getEmail();
        String imageUrl = photoService.uploadImage(request.getImageData(), email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PhotoUploadResponse("이미지 업로드 성공", imageUrl));
    }
}
