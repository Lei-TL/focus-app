package org.resourceserver.modules.stats.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StatsResponse {
    private Summary summary;
    private List<Achievement> achievements;
    private List<PerformanceStat> performanceStats;
    private WeeklyData weeklyData;

    @Data
    @Builder
    public static class Summary {
        private String weekFocus;
        private String streak;
        private String sessions;
        private String averagePerDay;
    }

    @Data
    @Builder
    public static class PerformanceStat {
        private String label;
        private String value;
        private String subValue;
        private String trend;
        private String icon;
        private String color;
    }

    @Data
    @Builder
    public static class Achievement {
        private String label;
        private String icon;
    }

    @Data
    @Builder
    public static class WeeklyData {
        private List<String> labels;
        private List<Integer> values;
    }
}
