package org.resourceserver.modules.stats.service.impl;

import lombok.RequiredArgsConstructor;
import org.resourceserver.modules.stats.dto.HistoryResponse;
import org.resourceserver.modules.stats.dto.StatsResponse;
import org.resourceserver.modules.stats.entity.FocusRecord;
import org.resourceserver.modules.stats.repository.FocusRecordRepository;
import org.resourceserver.modules.stats.service.StatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final FocusRecordRepository focusRecordRepository;

    @Override
    public StatsResponse getUserStats(String userId) {
        List<FocusRecord> records = focusRecordRepository.findByUserId(userId);

        Instant now = Instant.now();
        LocalDate today = now.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startOfWeekDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
        Instant startOfWeek = startOfWeekDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        long totalSecondsAllTime = 0;
        long totalSecondsToday = 0;
        long totalSecondsThisWeek = 0;

        for (FocusRecord record : records) {
            long seconds = record.getDurationSeconds();
            totalSecondsAllTime += seconds;

            LocalDate recordDate = record.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            if (recordDate.equals(today)) {
                totalSecondsToday += seconds;
            }

            if (record.getCreatedAt().isAfter(startOfWeek)) {
                totalSecondsThisWeek += seconds;
            }
        }

        long weekMins = totalSecondsThisWeek / 60;
        String weekFocus = (weekMins / 60) + "h " + (weekMins % 60) + "m";
        int streakCount = calculateStreakCount(records);

        long totalCompleted = records.stream().filter(FocusRecord::isCompleted).count();
        int activeDaysLast30 = calculateActiveDaysInLastN(records, 30);
        int totalActiveDays = calculateActiveDays(records);
        double avgMinsPerDay = (double) (totalSecondsAllTime / 60) / Math.max(1, totalActiveDays);

        return StatsResponse.builder()
                .summary(StatsResponse.Summary.builder()
                        .weekFocus(weekFocus)
                        .streak(streakCount + " days")
                        .sessions(String.valueOf(records.size()))
                        .averagePerDay((totalSecondsToday / 60) + "m")
                        .build())
                .performanceStats(Arrays.asList(
                        StatsResponse.PerformanceStat.builder()
                                .label("Average Focus")
                                .value(String.format("%.1f hrs/day", avgMinsPerDay / 60.0))
                                .icon("time-outline")
                                .color("#5B8DEF")
                                .build(),
                        StatsResponse.PerformanceStat.builder()
                                .label("Active Days")
                                .value((int) ((activeDaysLast30 / 30.0) * 100) + "%")
                                .subValue(activeDaysLast30 + " of 30d")
                                .icon("calendar-outline")
                                .color("#10B981")
                                .build(),
                        StatsResponse.PerformanceStat.builder()
                                .label("Completion")
                                .value((int) ((double) totalCompleted / Math.max(1, records.size()) * 100) + "%")
                                .subValue(totalCompleted + " finished")
                                .icon("checkmark-circle-outline")
                                .color("#F59E0B")
                                .build()
                ))
                .achievements(Arrays.asList(
                        StatsResponse.Achievement.builder().label("Consistent").icon("flame-outline").build(),
                        StatsResponse.Achievement.builder().label("Focused").icon("medal-outline").build()
                ))
                .weeklyData(StatsResponse.WeeklyData.builder()
                        .labels(Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
                        .values(calculateWeeklyValues(records, startOfWeek))
                        .build())
                .build();
    }

    private int calculateStreakCount(List<FocusRecord> records) {
        if (records.isEmpty()) return 0;
        
        List<LocalDate> activeDays = records.stream()
                .map(r -> r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                .distinct()
                .sorted((a, b) -> b.compareTo(a))
                .collect(Collectors.toList());
        
        if (activeDays.isEmpty()) return 0;
        
        LocalDate today = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate yesterday = today.minusDays(1);
        
        if (!activeDays.get(0).equals(today) && !activeDays.get(0).equals(yesterday)) {
            return 0;
        }
        
        int streak = 1;
        for (int i = 1; i < activeDays.size(); i++) {
            LocalDate previous = activeDays.get(i - 1);
            LocalDate current = activeDays.get(i);
            if (previous.minusDays(1).equals(current)) {
                streak++;
            } else {
                break;
            }
        }
        
        return streak;
    }

    private int calculateActiveDays(List<FocusRecord> records) {
        return (int) records.stream()
                .map(r -> r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                .distinct()
                .count();
    }

    private int calculateActiveDaysInLastN(List<FocusRecord> records, int n) {
        Instant limit = Instant.now().minus(java.time.Duration.ofDays(n));
        return (int) records.stream()
                .filter(r -> r.getCreatedAt().isAfter(limit))
                .map(r -> r.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                .distinct()
                .count();
    }

    private List<Integer> calculateWeeklyValues(List<FocusRecord> records, Instant startOfWeek) {
        Integer[] values = new Integer[7];
        Arrays.fill(values, 0);
        System.out.println("DEBUG: calculateWeeklyValues called with startOfWeek=" + startOfWeek);
        System.out.println("DEBUG: Number of records=" + records.size());

        for (FocusRecord r : records) {
            System.out.println("DEBUG: Record createdAt=" + r.getCreatedAt() + ", duration=" + r.getDurationSeconds() + "s");
            if (r.getCreatedAt().isAfter(startOfWeek) || r.getCreatedAt().equals(startOfWeek)) {
                int dayIdx = r.getCreatedAt().atZone(ZoneId.systemDefault()).getDayOfWeek().getValue() - 1;
                int minutes = (int) Math.round((double) r.getDurationSeconds() / 60.0);
                System.out.println("DEBUG: Adding " + minutes + " mins to dayIdx=" + dayIdx);
                values[dayIdx] += minutes;
            }
        }
        System.out.println("DEBUG: Final weekly values=" + Arrays.asList(values));
        return Arrays.asList(values);
    }

    @Override
    public HistoryResponse getUserHistory(String userId) {
        List<FocusRecord> records = focusRecordRepository.findByUserId(userId);

        long totalSeconds = records.stream().mapToLong(FocusRecord::getDurationSeconds).sum();

        List<HistoryResponse.SessionEntry> entries = records.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(r -> {
                    int mins = r.getDurationSeconds() / 60;
                    String durationStr = mins > 0 ? mins + " min" : r.getDurationSeconds() + " sec";

                    return HistoryResponse.SessionEntry.builder()
                            .id(r.getId())
                            .title(r.getMode() == FocusRecord.Mode.ROOM ? "Room Focus" : "Solo Focus")
                            .duration(durationStr)
                            .participants(r.getMode() == FocusRecord.Mode.ROOM ? 2 : 1) // Approx
                            .date(formatDate(r.getCreatedAt()))
                            .completed(r.isCompleted())
                            .build();
                })
                .collect(Collectors.toList());

        return HistoryResponse.builder()
                .summary(HistoryResponse.Summary.builder()
                        .totalFocus((totalSeconds / 3600) + "h " + ((totalSeconds % 3600) / 60) + "m")
                        .totalSessions(records.size())
                        .totalDays(calculateActiveDays(records))
                        .build())
                .sessions(entries)
                .build();
    }

    private String formatDate(Instant instant) {
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
        if (date.equals(today)) return "Today";
        if (date.equals(today.minusDays(1))) return "Yesterday";
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    @Override
    @Transactional
    public FocusRecord saveSoloRecord(String userId, int durationSeconds, boolean completed) {
        double completionRate = completed ? 1.0 : (double) durationSeconds / (25 * 60);
        completionRate = Math.min(1.0, completionRate);

        FocusRecord record = FocusRecord.builder()
                .userId(userId)
                .mode(FocusRecord.Mode.SOLO)
                .durationSeconds(durationSeconds)
                .completed(completed)
                .completionRate(completionRate)
                .startedAt(Instant.now().minusSeconds(durationSeconds))
                .endedAt(Instant.now())
                .build();

        return focusRecordRepository.save(record);
    }

    @Override
    @Transactional
    public FocusRecord saveRoomRecord(String userId, Long roomId, String sessionId, int durationSeconds, boolean completed) {
        double completionRate = completed ? 1.0 : (double) durationSeconds / (25 * 60);
        completionRate = Math.min(1.0, completionRate);

        FocusRecord record = FocusRecord.builder()
                .userId(userId)
                .roomId(roomId)
                .sessionId(sessionId)
                .mode(FocusRecord.Mode.ROOM)
                .durationSeconds(durationSeconds)
                .completed(completed)
                .completionRate(completionRate)
                .startedAt(Instant.now().minusSeconds(durationSeconds))
                .endedAt(Instant.now())
                .build();

        return focusRecordRepository.save(record);
    }
}
