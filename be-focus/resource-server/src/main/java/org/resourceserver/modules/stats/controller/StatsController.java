package org.resourceserver.modules.stats.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.resourceserver.common.response.ApiResponse;
import org.resourceserver.modules.stats.dto.HistoryResponse;
import org.resourceserver.modules.stats.dto.StatsResponse;
import org.resourceserver.modules.stats.entity.FocusRecord;
import org.resourceserver.modules.stats.service.StatsService;
import org.resourceserver.modules.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<StatsResponse> getStats() {
        String userId = userService.getCurrentUserId();
        return ApiResponse.success(statsService.getUserStats(userId));
    }

    @GetMapping("/history")
    public ApiResponse<HistoryResponse> getHistory() {
        String userId = userService.getCurrentUserId();
        return ApiResponse.success(statsService.getUserHistory(userId));
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SaveSoloRequest {
        private int durationSeconds;
        private boolean completed;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SaveRoomRequest {
        private Long roomId;
        private String sessionId;
        private int durationSeconds;
        private boolean completed;
    }

    @PostMapping("/solo")
    public ApiResponse<FocusRecord> saveSolo(@RequestBody SaveSoloRequest request) {
        String userId = userService.getCurrentUserId();
        FocusRecord record = statsService.saveSoloRecord(userId, request.getDurationSeconds(), request.isCompleted());
        return ApiResponse.success(record);
    }

    @PostMapping("/room")
    public ApiResponse<FocusRecord> saveRoom(@RequestBody SaveRoomRequest request) {
        String userId = userService.getCurrentUserId();
        FocusRecord record = statsService.saveRoomRecord(userId, request.getRoomId(), request.getSessionId(), request.getDurationSeconds(), request.isCompleted());
        return ApiResponse.success(record);
    }
}
