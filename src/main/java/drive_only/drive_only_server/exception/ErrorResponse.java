package drive_only.drive_only_server.exception;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;

public record ErrorResponse(
        String code,
        String message
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
