package org.resourceserver.modules.session.service;

import org.resourceserver.modules.session.entity.SessionParticipant;

import java.util.List;
import java.util.Optional;

public interface SessionParticipantService {
    SessionParticipant addParticipant(String sessionId, String userId);
    Optional<SessionParticipant> getParticipant(String sessionId, String userId);
    List<SessionParticipant> getParticipantsBySessionId(String sessionId);
    long countParticipantsBySessionId(String sessionId);
}
