package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidQueryCombinationException extends RuntimeException {
    private final ErrorCode errorCode;

    public InvalidQueryCombinationException() {
        super(ErrorCode.INVALID_QUERY_COMBINATION.getMessage());
        this.errorCode = ErrorCode.INVALID_QUERY_COMBINATION;
    }
}
