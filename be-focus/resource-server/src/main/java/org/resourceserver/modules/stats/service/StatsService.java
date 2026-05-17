package org.resourceserver.modules.stats.service;

import org.resourceserver.modules.stats.dto.HistoryResponse;
import org.resourceserver.modules.stats.dto.StatsResponse;

public interface StatsService {
    StatsResponse getUserStats(String userId);
    HistoryResponse getUserHistory(String userId);
}
