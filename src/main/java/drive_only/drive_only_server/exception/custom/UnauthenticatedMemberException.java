package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class UnauthenticatedMemberException extends RuntimeException {
    private final ErrorCode errorCode;

    public UnauthenticatedMemberException() {
        super(ErrorCode.UNAUTHENTICATED_MEMBER.getMessage());
        this.errorCode = ErrorCode.UNAUTHENTICATED_MEMBER;
    }
}
