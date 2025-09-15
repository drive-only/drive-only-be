package drive_only.drive_only_server.controller.photo;

import drive_only.drive_only_server.dto.common.ApiResult;
import drive_only.drive_only_server.dto.common.ApiResultSupport;
import drive_only.drive_only_server.exception.annotation.ApiErrorCodeExamples;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.security.CustomUserPrincipal;
import drive_only.drive_only_server.service.photo.PhotoService;
import drive_only.drive_only_server.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "사진", description = "사진 업로드 API")
public class PhotoController {
    private final PhotoService photoService;

    @Operation(summary = "multipart 업로드", description = "multipart/form-data 파일 업로드 후 temp 경로에 저장, URL 반환")
    @ApiErrorCodeExamples({
            ErrorCode.UNAUTHENTICATED_MEMBER,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.INVALID_IMAGE_DATA,
            ErrorCode.FILE_UPLOAD_FAIL,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping(path= "/api/photos", consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResult<Map<String,String>>> uploadTemp(
            @RequestPart("image") MultipartFile file,
            @AuthenticationPrincipal CustomUserPrincipal user
    ){
        if (user == null) throw new BusinessException(ErrorCode.UNAUTHENTICATED_MEMBER);
        var ur = photoService.uploadTemp(file, user.getEmail());
        return ApiResultSupport.ok(SuccessCode.SUCCESS_UPLOAD_TEMP_IMAGE, Map.of("url", ur));
    }
}
