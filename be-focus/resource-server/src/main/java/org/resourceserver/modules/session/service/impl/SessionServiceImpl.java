package org.resourceserver.modules.session.service.impl;

import lombok.RequiredArgsConstructor;
import org.resourceserver.modules.room.entity.Participant;
import org.resourceserver.modules.room.entity.ParticipantStatus;
import org.resourceserver.modules.room.repository.ParticipantRepository;
import org.resourceserver.modules.session.dto.SoloSessionRequest;
import org.resourceserver.modules.session.entity.Session;
import org.resourceserver.modules.session.repository.SessionRepository;
import org.resourceserver.modules.session.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final ParticipantRepository participantRepository;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public void saveSoloSession(SoloSessionRequest request) {
        String sessionId = UUID.randomUUID().toString();
        int durationMinutes = request.getDurationSeconds() / 60;

        Session session = Session.builder()
                .id(sessionId)
                .durationMinutes(durationMinutes)
                .startTime(LocalDateTime.now().minusSeconds(request.getDurationSeconds()))
                .endTime(LocalDateTime.now())
                .status(request.isCompleted() ? Session.SessionStatus.COMPLETED : Session.SessionStatus.ENDED)
                .build();
        sessionRepository.save(session);

        Participant participant = Participant.builder()
                .userId(request.getUserId())
                .sessionId(sessionId)
                .status(request.isCompleted() ? ParticipantStatus.COMPLETED : ParticipantStatus.LEFT)
                .joinTime(LocalDateTime.now().minusSeconds(request.getDurationSeconds()))
                .leaveTime(LocalDateTime.now())
                .completed(request.isCompleted())
                .build();

        participantRepository.save(participant);
    }
}
