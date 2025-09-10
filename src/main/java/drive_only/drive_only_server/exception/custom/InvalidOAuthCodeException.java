package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidOAuthCodeException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.INVALID_OAUTH_CODE;

    public InvalidOAuthCodeException() {
        super(ErrorCode.INVALID_OAUTH_CODE.getMessage());
    }

    public InvalidOAuthCodeException(Throwable cause) {
        super(ErrorCode.INVALID_OAUTH_CODE.getMessage(), cause);
    }
}
