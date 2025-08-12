package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class MemberNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.MEMBER_NOT_FOUND;
    }
}
