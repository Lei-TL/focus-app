package org.resourceserver.modules.session.service;

import org.resourceserver.modules.session.entity.FocusSession;

import java.util.List;
import java.util.Optional;

public interface FocusSessionService {
    FocusSession createSession(Long roomId, String startedBy, int durationSeconds);
    Optional<FocusSession> getActiveSession(Long roomId);
    List<FocusSession> getSessionsByRoomId(Long roomId);
    void endSession(String sessionId);
}
