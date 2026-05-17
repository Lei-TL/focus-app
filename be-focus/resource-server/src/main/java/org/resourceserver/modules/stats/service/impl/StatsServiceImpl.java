package org.resourceserver.modules.stats.service.impl;

import lombok.RequiredArgsConstructor;
import org.resourceserver.modules.room.entity.Participant;
import org.resourceserver.modules.room.repository.ParticipantRepository;
import org.resourceserver.modules.stats.dto.HistoryResponse;
import org.resourceserver.modules.stats.dto.StatsResponse;
import org.resourceserver.modules.stats.service.StatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final ParticipantRepository participantRepository;

    @Override
    public StatsResponse getUserStats(String userId) {
        List<Participant> allSessions = participantRepository.findByUserId(userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0).withSecond(0);

        long totalMinutesAllTime = 0;
        long totalMinutesToday = 0;
        long totalMinutesThisWeek = 0;

        for (Participant p : allSessions) {
            long seconds = p.isCompleted() ?
                    (p.getRoom() != null ? p.getRoom().getDefaultDuration() * 60L : 25 * 60L) :
                    (p.getJoinTime() != null && p.getLeaveTime() != null ?
                            java.time.Duration.between(p.getJoinTime(), p.getLeaveTime()).getSeconds() : 0);

            long mins = seconds / 60;
            totalMinutesAllTime += mins;

            if (p.getJoinTime().toLocalDate().equals(now.toLocalDate())) {
                totalMinutesToday += mins;
            }

            if (p.getJoinTime().isAfter(startOfWeek)) {
                totalMinutesThisWeek += mins;
            }
        }

        String weekFocus = (totalMinutesThisWeek / 60) + "h " + (totalMinutesThisWeek % 60) + "m";
        int streakCount = calculateStreakCount(allSessions);

        long totalCompleted = allSessions.stream().filter(Participant::isCompleted).count();
        int activeDaysLast30 = calculateActiveDaysInLastN(allSessions, 30);
        double avgMinsPerDay = (double) totalMinutesAllTime / Math.max(1, calculateActiveDays(allSessions));

        return StatsResponse.builder()
                .summary(StatsResponse.Summary.builder()
                        .weekFocus(weekFocus)
                        .streak(streakCount + (streakCount == 1 ? " day" : " days"))
                        .sessions(String.valueOf(allSessions.size()))
                        .averagePerDay(totalMinutesToday + "m")
                        .build())
                .performanceStats(Arrays.asList(
                        StatsResponse.PerformanceStat.builder()
                                .label("Average Focus Time")
                                .value(String.format("%.1f hours/day", avgMinsPerDay / 60.0))
                                .subValue(String.format("%.1f hours/day", avgMinsPerDay / 60.0))
                                .trend("↑ 12%")
                                .icon("time-outline")
                                .color("#5B8DEF")
                                .build(),
                        StatsResponse.PerformanceStat.builder()
                                .label("Active Days")
                                .value((int) ((activeDaysLast30 / 30.0) * 100) + "%")
                                .subValue(activeDaysLast30 + " of last 30 days")
                                .icon("calendar-outline")
                                .color("#10B981")
                                .build(),
                        StatsResponse.PerformanceStat.builder()
                                .label("Completion Rate")
                                .value((int) ((double) totalCompleted / Math.max(1, allSessions.size()) * 100) + "%")
                                .subValue(totalCompleted + " of " + allSessions.size() + " sessions")
                                .icon("checkmark-circle-outline")
                                .color("#F59E0B")
                                .build()
                ))
                .achievements(Arrays.asList(
                        StatsResponse.Achievement.builder().label("First Step").icon("medal-outline").build(),
                        StatsResponse.Achievement.builder().label("Consistent").icon("flame-outline").build(),
                        StatsResponse.Achievement.builder().label("Deep Work").icon("fitness-outline").build()
                ))
                .weeklyData(StatsResponse.WeeklyData.builder()
                        .labels(Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
                        .values(calculateWeeklyValues(allSessions, startOfWeek))
                        .build())
                .build();
    }

    private int calculateStreakCount(List<Participant> sessions) {
        if (sessions.isEmpty()) return 0;
        return calculateActiveDays(sessions);
    }

    private int calculateActiveDays(List<Participant> sessions) {
        return (int) sessions.stream()
                .map(p -> p.getJoinTime().toLocalDate())
                .distinct()
                .count();
    }

    private int calculateActiveDaysInLastN(List<Participant> sessions, int n) {
        LocalDateTime limit = LocalDateTime.now().minusDays(n);
        return (int) sessions.stream()
                .filter(p -> p.getJoinTime().isAfter(limit))
                .map(p -> p.getJoinTime().toLocalDate())
                .distinct()
                .count();
    }

    private List<Double> calculateWeeklyValues(List<Participant> sessions, LocalDateTime startOfWeek) {
        Double[] values = new Double[7];
        Arrays.fill(values, 0.0);

        for (Participant p : sessions) {
            if (p.getJoinTime().isAfter(startOfWeek)) {
                int dayIdx = p.getJoinTime().getDayOfWeek().getValue() - 1;
                long mins = p.isCompleted() ?
                        (p.getRoom() != null ? p.getRoom().getDefaultDuration() : 25) :
                        java.time.Duration.between(p.getJoinTime(), p.getLeaveTime()).toMinutes();
                values[dayIdx] += (double) mins / 60.0;
            }
        }
        return Arrays.asList(values);
    }

    @Override
    public HistoryResponse getUserHistory(String userId) {
        List<Participant> allSessions = participantRepository.findByUserId(userId);

        long totalMinutes = allSessions.stream()
                .mapToLong(p -> {
                    if (p.isCompleted()) {
                        return p.getRoom() != null ? p.getRoom().getDefaultDuration() : 25;
                    } else if (p.getJoinTime() != null && p.getLeaveTime() != null) {
                        return java.time.Duration.between(p.getJoinTime(), p.getLeaveTime()).toMinutes();
                    }
                    return 0;
                })
                .sum();

        List<HistoryResponse.SessionEntry> entries = allSessions.stream()
                .sorted((a, b) -> b.getJoinTime().compareTo(a.getJoinTime()))
                .map(p -> {
                    long seconds = p.isCompleted() ?
                            (p.getRoom() != null ? p.getRoom().getDefaultDuration() * 60L : 25 * 60L) :
                            (p.getJoinTime() != null && p.getLeaveTime() != null ?
                                    java.time.Duration.between(p.getJoinTime(), p.getLeaveTime()).getSeconds() : 0);

                    long mins = seconds / 60;
                    String durationStr = mins > 0 ? mins + " min" : seconds + " sec";

                    return HistoryResponse.SessionEntry.builder()
                            .id(p.getId())
                            .title((p.getRoom() != null ? p.getRoom().getName() : "Solo Focus") + (p.isCompleted() ? "" : " (Canceled)"))
                            .duration(durationStr)
                            .participants(p.getRoom() != null ? p.getRoom().getCurrentParticipants() : 1)
                            .date(p.getJoinTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                            .build();
                })
                .collect(Collectors.toList());

        return HistoryResponse.builder()
                .summary(HistoryResponse.Summary.builder()
                        .totalFocus((totalMinutes / 60) + "h " + (totalMinutes % 60) + "m")
                        .totalSessions(allSessions.size())
                        .totalDays(1)
                        .build())
                .sessions(entries)
                .build();
    }
}
