package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class CommentNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.COMMENT_NOT_FOUND;
    }
}
