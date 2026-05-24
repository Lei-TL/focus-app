package org.resourceserver.modules.stats.service;

import org.resourceserver.modules.stats.dto.HistoryResponse;
import org.resourceserver.modules.stats.dto.StatsResponse;
import org.resourceserver.modules.stats.entity.FocusRecord;

public interface StatsService {
    StatsResponse getUserStats(String userId);
    HistoryResponse getUserHistory(String userId);
    FocusRecord saveSoloRecord(String userId, int durationSeconds, boolean completed);
    FocusRecord saveRoomRecord(String userId, Long roomId, String sessionId, int durationSeconds, boolean completed);
}
