package org.resourceserver.realtime.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resourceserver.common.response.AppEvent;
import org.resourceserver.realtime.websocket.dto.WsMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String roomIdStr = getRoomId(session);
        if (roomIdStr != null) {
            Long roomId = Long.parseLong(roomIdStr);
            roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("New WebSocket connection for room {}: {}", roomId, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomIdStr = getRoomId(session);
        if (roomIdStr != null) {
            Long roomId = Long.parseLong(roomIdStr);
            Set<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    roomSessions.remove(roomId);
                }
            }
        }
    }

    public void broadcastToRoom(Long roomId, AppEvent event, Object data) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null && !sessions.isEmpty()) {
            WsMessage<Object> message = WsMessage.builder()
                    .event(event)
                    .data(data)
                    .build();

            try {
                String payload = objectMapper.writeValueAsString(message);
                TextMessage textMessage = new TextMessage(payload);

                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                }
            } catch (IOException e) {
                log.error("Error broadcasting message to room {}: {}", roomId, e.getMessage());
            }
        }
    }

    private String getRoomId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("roomId=")) {
            return query.split("=")[1];
        }
        return null;
    }
}
