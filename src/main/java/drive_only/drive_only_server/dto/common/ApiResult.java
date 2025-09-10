package drive_only.drive_only_server.dto.common;

public record ApiResult<T>(
        String code,
        String message,
        T result
) {
    public static <T> ApiResult<T> of(String code, String message, T result) {
        return new ApiResult<>(code, message, result);
    }
}
