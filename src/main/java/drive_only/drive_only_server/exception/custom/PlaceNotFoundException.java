package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class PlaceNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public PlaceNotFoundException() {
        super(ErrorCode.PLACE_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.PLACE_NOT_FOUND;
    }
}
