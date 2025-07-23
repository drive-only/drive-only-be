package drive_only.drive_only_server.exception.custom;

import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class CourseNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public CourseNotFoundException() {
        super(ErrorCode.COURSE_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.COURSE_NOT_FOUND;
    }
}
