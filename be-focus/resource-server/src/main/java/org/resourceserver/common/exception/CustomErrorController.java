package org.resourceserver.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resourceserver.common.response.ApiResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @RequestMapping(ERROR_PATH)
    public ResponseEntity<ApiResponse<?>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        Object message = request.getAttribute("jakarta.servlet.error.message");
        Object exception = request.getAttribute("jakarta.servlet.error.exception");

        int statusCode = status != null ? (int) status : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String errorMessage = message != null ? message.toString() : "An error occurred";

        if (exception instanceof ResponseStatusException responseStatusException) {
            statusCode = responseStatusException.getStatusCode().value();
            errorMessage = responseStatusException.getReason() != null ? responseStatusException.getReason() : responseStatusException.getMessage();
        } else if (exception instanceof Throwable throwable) {
            errorMessage = throwable.getMessage() != null ? throwable.getMessage() : errorMessage;
        }

        log.error("Error occurred: status={}, message={}", statusCode, errorMessage);

        return ResponseEntity
                .status(statusCode)
                .body(ApiResponse.error(statusCode, errorMessage));
    }
}
