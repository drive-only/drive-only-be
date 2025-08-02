package drive_only.drive_only_server.exception;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답")
public record ErrorResponse(
        @Schema(description = "에러 코드", example = "INVALID_TOKEN")
        String code,

        @Schema(description = "에러 메시지", example = "유효하지 않은 토큰입니다.")
        String message,
        int status
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getStatus().value()
        );
    }
}
