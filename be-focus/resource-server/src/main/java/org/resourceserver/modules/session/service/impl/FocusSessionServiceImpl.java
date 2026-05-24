package org.resourceserver.modules.session.service.impl;

import lombok.RequiredArgsConstructor;
import org.resourceserver.common.events.SessionEndedEvent;
import org.resourceserver.modules.room.entity.Room;
import org.resourceserver.modules.room.repository.RoomRepository;
import org.resourceserver.modules.session.entity.FocusSession;
import org.resourceserver.modules.session.entity.SessionParticipant;
import org.resourceserver.modules.session.repository.FocusSessionRepository;
import org.resourceserver.modules.session.repository.SessionParticipantRepository;
import org.resourceserver.modules.session.service.FocusSessionService;
import org.resourceserver.modules.session.service.SessionParticipantService;
import org.resourceserver.modules.stats.service.StatsService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FocusSessionServiceImpl implements FocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final RoomRepository roomRepository;
    private final StatsService statsService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public FocusSession createSession(Long roomId, String startedBy, int durationSeconds) {
        FocusSession session = FocusSession.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .status(FocusSession.SessionStatus.FOCUSING)
                .startedBy(startedBy)
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(durationSeconds))
                .durationSeconds(durationSeconds)
                .build();
        return focusSessionRepository.save(session);
    }

    @Override
    public Optional<FocusSession> getActiveSession(Long roomId) {
        return focusSessionRepository.findFirstByRoomIdAndStatusOrderByCreatedAtDesc(
                roomId,
                FocusSession.SessionStatus.FOCUSING
        );
    }

    @Override
    public List<FocusSession> getSessionsByRoomId(Long roomId) {
        return focusSessionRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional
    public void endSession(String sessionId) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != FocusSession.SessionStatus.FOCUSING) {
            return;
        }

        session.setStatus(FocusSession.SessionStatus.COMPLETED);
        session.setActualEndTime(Instant.now());
        focusSessionRepository.save(session);

        Room room = roomRepository.findById(session.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setCurrentSessionId(null);
        roomRepository.save(room);

        List<SessionParticipant> participants = sessionParticipantRepository.findBySessionId(sessionId);
        for (SessionParticipant participant : participants) {
            if (participant.getStatus() != SessionParticipant.ParticipantStatus.LEFT_EARLY) {
                participant.setStatus(SessionParticipant.ParticipantStatus.COMPLETED);
                participant.setCompletedAt(Instant.now());
                participant.setCompleted(true);
                int focusDurationSeconds = session.getDurationSeconds();
                participant.setFocusDurationSeconds(focusDurationSeconds);
                participant.setCompletionRate(1.0);
            }

            statsService.saveRoomRecord(
                    participant.getUserId(),
                    session.getRoomId(),
                    session.getId(),
                    participant.getFocusDurationSeconds(),
                    participant.isCompleted()
            );
        }

        eventPublisher.publishEvent(new SessionEndedEvent(this, session.getRoomId(), session.getId()));
    }
}
