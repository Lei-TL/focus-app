package org.resourceserver.realtime.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resourceserver.common.response.AppEvent;
import org.resourceserver.realtime.presence.PresenceService;
import org.resourceserver.realtime.websocket.dto.WsMessage;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final PresenceService presenceService;

    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, String> queryParams = getQueryParams(session);
        String roomIdStr = queryParams.get("roomId");
        String userId = queryParams.get("userId");

        if (roomIdStr != null) {
            Long roomId = Long.parseLong(roomIdStr);
            roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);

            if (userId != null) {
                presenceService.markUserOnline(roomId, userId);
                session.getAttributes().put("userId", userId);
                session.getAttributes().put("roomId", roomIdStr);
            }


        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomIdStr = (String) session.getAttributes().get("roomId");
        String userId = (String) session.getAttributes().get("userId");

        if (roomIdStr != null) {
            Long roomId = Long.parseLong(roomIdStr);
            Set<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    roomSessions.remove(roomId);
                }
            }

            if (userId != null) {
                presenceService.markUserOffline(roomId, userId);
            }
        }
    }

    public void broadcastToRoom(Long roomId, AppEvent event, Object data) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null && !sessions.isEmpty()) {
            WsMessage<Object> message = WsMessage.builder()
                    .event(event)
                    .data(data)
                    .roomId(roomId)
                    .timestamp(System.currentTimeMillis())
                    .version(1)
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
                log.error("Error broadcasting message to room {}: {}", roomId, e.getMessage(), e);
            }
        }
    }

    @Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        roomSessions.values().forEach(sessions -> {
            sessions.forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage("{\"event\":\"PING\"}"));
                    } catch (IOException e) {
                        log.warn("Failed to send heartbeat to session {}", session.getId());
                    }
                }
            });
        });
    }

    private Map<String, String> getQueryParams(WebSocketSession session) {
        Map<String, String> queryParams = new ConcurrentHashMap<>();
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] idx = pair.split("=");
                if (idx.length > 1) {
                    queryParams.put(idx[0], idx[1]);
                }
            }
        }
        return queryParams;
    }
}
