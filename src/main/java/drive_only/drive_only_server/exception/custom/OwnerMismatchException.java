package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class OwnerMismatchException extends RuntimeException{
    private final ErrorCode errorCode;

    public OwnerMismatchException() {
        this.errorCode = ErrorCode.OWNER_MISMATCH;
    }
}
