package org.resourceserver.modules.stats.controller;

import lombok.RequiredArgsConstructor;
import org.resourceserver.common.response.ApiResponse;
import org.resourceserver.modules.stats.dto.HistoryResponse;
import org.resourceserver.modules.stats.dto.StatsResponse;
import org.resourceserver.modules.stats.service.StatsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping
    public ApiResponse<StatsResponse> getStats(@RequestParam String userId) {
        return ApiResponse.success(statsService.getUserStats(userId));
    }

    @GetMapping("/history")
    public ApiResponse<HistoryResponse> getHistory(@RequestParam String userId) {
        return ApiResponse.success(statsService.getUserHistory(userId));
    }
}
