package org.resourceserver.modules.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resourceserver.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    @GetMapping("/error")
    public ApiResponse<String> testError() {
        log.info("Test error endpoint called");
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Test error message");
    }

    @GetMapping("/success")
    public ApiResponse<String> testSuccess() {
        log.info("Test success endpoint called");
        return ApiResponse.success("Hello World!");
    }
}
