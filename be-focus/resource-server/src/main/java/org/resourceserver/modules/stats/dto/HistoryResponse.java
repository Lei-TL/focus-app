package org.resourceserver.modules.stats.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class HistoryResponse {
    private Summary summary;
    private List<SessionEntry> sessions;

    @Data
    @Builder
    public static class Summary {
        private String totalFocus;
        private int totalSessions;
        private int totalDays;
    }

    @Data
    @Builder
    public static class SessionEntry {
        private Long id;
        private String title;
        private String duration;
        private int participants;
        private String date;
    }
}
