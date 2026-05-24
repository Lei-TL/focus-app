package org.resourceserver.modules.session.service.impl;

import lombok.RequiredArgsConstructor;
import org.resourceserver.modules.session.entity.SessionParticipant;
import org.resourceserver.modules.session.repository.SessionParticipantRepository;
import org.resourceserver.modules.session.service.SessionParticipantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionParticipantServiceImpl implements SessionParticipantService {

    private final SessionParticipantRepository sessionParticipantRepository;

    @Override
    @Transactional
    public SessionParticipant addParticipant(String sessionId, String userId) {
        Optional<SessionParticipant> existing = sessionParticipantRepository
                .findBySessionIdAndUserId(sessionId, userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        SessionParticipant participant = SessionParticipant.builder()
                .sessionId(sessionId)
                .userId(userId)
                .status(SessionParticipant.ParticipantStatus.FOCUSING)
                .completed(false)
                .focusDurationSeconds(0)
                .completionRate(0.0)
                .build();
        return sessionParticipantRepository.save(participant);
    }

    @Override
    public Optional<SessionParticipant> getParticipant(String sessionId, String userId) {
        return sessionParticipantRepository.findBySessionIdAndUserId(sessionId, userId);
    }

    @Override
    public List<SessionParticipant> getParticipantsBySessionId(String sessionId) {
        return sessionParticipantRepository.findBySessionId(sessionId);
    }

    @Override
    public long countParticipantsBySessionId(String sessionId) {
        return sessionParticipantRepository.countBySessionId(sessionId);
    }
}
