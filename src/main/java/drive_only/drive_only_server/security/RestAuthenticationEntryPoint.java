package drive_only.drive_only_server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import drive_only.drive_only_server.exception.ErrorResponse;
import drive_only.drive_only_server.exception.errorcode.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

// 401 전담
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Object ec = request.getAttribute("ERROR_CODE");
        ErrorCode errorCode = (ec instanceof ErrorCode) ? (ErrorCode) ec : null;

        if (errorCode == null) {
            Throwable cause = authException.getCause();
            if (cause instanceof ExpiredJwtException) {
                errorCode = ErrorCode.TOKEN_EXPIRED;
            } else {
                errorCode = ErrorCode.UNAUTHENTICATED_MEMBER; // 기본값
            }
        }

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode));
    }
}
