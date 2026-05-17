package org.resourceserver.modules.session.controller;

import lombok.RequiredArgsConstructor;
import org.resourceserver.common.response.ApiResponse;
import org.resourceserver.modules.session.dto.SoloSessionRequest;
import org.resourceserver.modules.session.service.SessionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @PostMapping("/solo")
    public ApiResponse<Void> saveSoloSession(@RequestBody SoloSessionRequest request) {
        sessionService.saveSoloSession(request);
        return ApiResponse.success(null);
    }
}
