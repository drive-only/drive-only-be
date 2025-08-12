package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class RefreshTokenNotFoundException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.REFRESH_TOKEN_NOT_FOUND;

    public RefreshTokenNotFoundException() {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }
}
