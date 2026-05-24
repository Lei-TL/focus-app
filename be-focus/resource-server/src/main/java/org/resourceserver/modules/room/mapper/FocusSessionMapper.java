package org.resourceserver.modules.room.mapper;

import lombok.RequiredArgsConstructor;
import org.resourceserver.modules.room.dto.SessionInfoResponse;
import org.resourceserver.modules.room.dto.SessionParticipantResponse;
import org.resourceserver.modules.session.entity.FocusSession;
import org.resourceserver.modules.session.entity.SessionParticipant;
import org.resourceserver.modules.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FocusSessionMapper {

    private final SessionParticipantMapper sessionParticipantMapper;

    public SessionInfoResponse toSessionInfoResponse(
            FocusSession session,
            List<SessionParticipant> participants,
            Map<String, User> userMap
    ) {
        int remainingSeconds = 0;
        if (session.getStatus() == FocusSession.SessionStatus.FOCUSING && session.getEndTime() != null) {
            remainingSeconds = (int) Duration.between(Instant.now(), session.getEndTime()).getSeconds();
            remainingSeconds = Math.max(0, remainingSeconds);
        }

        List<SessionParticipantResponse> participantResponses = participants.stream()
                .map(p -> sessionParticipantMapper.toSessionParticipantResponse(p, userMap.get(p.getUserId())))
                .collect(Collectors.toList());

        return SessionInfoResponse.builder()
                .id(session.getId())
                .roomId(session.getRoomId())
                .status(session.getStatus().name())
                .startedBy(session.getStartedBy())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .actualEndTime(session.getActualEndTime())
                .durationSeconds(session.getDurationSeconds())
                .remainingSeconds(remainingSeconds)
                .participants(participantResponses)
                .build();
    }
}
