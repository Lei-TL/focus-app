package org.resourceserver.modules.session.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resourceserver.common.response.AppEvent;
import org.resourceserver.modules.room.dto.RoomResponse;
import org.resourceserver.modules.room.mapper.RoomMapper;
import org.resourceserver.modules.room.repository.RoomRepository;
import org.resourceserver.modules.session.entity.FocusSession;
import org.resourceserver.modules.session.repository.FocusSessionRepository;
import org.resourceserver.modules.session.service.FocusSessionService;
import org.resourceserver.realtime.websocket.handler.RoomWebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FocusSessionScheduler {

    private final FocusSessionRepository focusSessionRepository;
    private final FocusSessionService focusSessionService;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final RoomWebSocketHandler webSocketHandler;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void checkAndEndExpiredSessions() {
        List<FocusSession> activeSessions = focusSessionRepository.findByStatus(FocusSession.SessionStatus.FOCUSING);
        Instant now = Instant.now();

        for (FocusSession session : activeSessions) {
            if (session.getEndTime() != null && session.getEndTime().isBefore(now)) {
                log.info("Auto-ending expired session: {}", session.getId());
                try {
                    focusSessionService.endSession(session.getId());
                    roomRepository.findById(session.getRoomId()).ifPresent(room -> {
                        RoomResponse response = roomMapper.toRoomResponse(room);
                        webSocketHandler.broadcastToRoom(session.getRoomId(), AppEvent.SESSION_COMPLETED, response);
                    });
                } catch (Exception e) {
                    log.error("Error ending expired session: {}", session.getId(), e);
                }
            }
        }
    }
}
