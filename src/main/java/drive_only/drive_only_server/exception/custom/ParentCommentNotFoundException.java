package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class ParentCommentNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public ParentCommentNotFoundException() {
        super(ErrorCode.PARENT_COMMENT_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.PARENT_COMMENT_NOT_FOUND;
    }
}
