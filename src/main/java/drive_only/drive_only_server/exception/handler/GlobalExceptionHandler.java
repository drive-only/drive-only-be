package drive_only.drive_only_server.exception.handler;

import drive_only.drive_only_server.exception.custom.BusinessException;
import drive_only.drive_only_server.exception.custom.CommentNotFoundException;
import drive_only.drive_only_server.exception.custom.ParentCommentNotFoundException;
import drive_only.drive_only_server.exception.custom.UnauthenticatedMemberException;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import drive_only.drive_only_server.exception.ErrorResponse;
import drive_only.drive_only_server.exception.custom.CourseNotFoundException;
import drive_only.drive_only_server.exception.custom.PlaceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        return toResponse(exception.getErrorCode());
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCourseNotFoundException(CourseNotFoundException exception) {
        return toResponse(exception.getErrorCode());
    }

    @ExceptionHandler(PlaceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlaceNotFoundException(PlaceNotFoundException exception) {
        return toResponse(exception.getErrorCode());
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFoundException(CommentNotFoundException exception) {
        return toResponse(exception.getErrorCode());
    }

    @ExceptionHandler(ParentCommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleParentCommentNotFoundException(ParentCommentNotFoundException exception) {
        return toResponse(exception.getErrorCode());
    }

    @ExceptionHandler(UnauthenticatedMemberException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticatedMemberException(UnauthenticatedMemberException exception) {
        return toResponse(exception.getErrorCode());
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }
}
