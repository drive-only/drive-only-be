package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class OAuthCommunicationException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.OAUTH_COMMUNICATION_FAILED;

    public OAuthCommunicationException(String reason) {
        super(reason);
    }

    public OAuthCommunicationException(String reason, Throwable cause) {
        super(reason, cause);
    }
}