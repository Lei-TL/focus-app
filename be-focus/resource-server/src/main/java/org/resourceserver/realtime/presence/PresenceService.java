package org.resourceserver.realtime.presence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PRESENCE_KEY_PREFIX = "presence:room:";
    private static final long PRESENCE_TTL_SECONDS = 60;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserPresence {
        private String userId;
        private String socketId;
        private LocalDateTime lastPing;
        private String status;
    }

    public void updatePresence(Long roomId, String userId, String socketId) {
        String key = PRESENCE_KEY_PREFIX + roomId;
        UserPresence presence = UserPresence.builder()
                .userId(userId)
                .socketId(socketId)
                .lastPing(LocalDateTime.now())
                .status("ONLINE")
                .build();
        redisTemplate.opsForHash().put(key, userId, presence);
        redisTemplate.expire(key, PRESENCE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    public void removePresence(Long roomId, String userId) {
        String key = PRESENCE_KEY_PREFIX + roomId;
        redisTemplate.opsForHash().delete(key, userId);
    }

    public List<UserPresence> getOnlineUsersInRoom(Long roomId) {
        String key = PRESENCE_KEY_PREFIX + roomId;
        Set<Object> keys = redisTemplate.opsForHash().keys(key);
        List<UserPresence> presences = new ArrayList<>();
        for (Object k : keys) {
            UserPresence presence = (UserPresence) redisTemplate.opsForHash().get(key, k);
            if (presence != null && isPresenceActive(presence)) {
                presences.add(presence);
            }
        }
        return presences;
    }

    public void markUserOnline(Long roomId, String userId) {
        String key = PRESENCE_KEY_PREFIX + roomId;
        UserPresence presence = UserPresence.builder()
                .userId(userId)
                .socketId(null)
                .lastPing(LocalDateTime.now())
                .status("ONLINE")
                .build();
        redisTemplate.opsForHash().put(key, userId, presence);
        redisTemplate.expire(key, PRESENCE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    public void markUserOffline(Long roomId, String userId) {
        String key = PRESENCE_KEY_PREFIX + roomId;
        redisTemplate.opsForHash().delete(key, userId);
    }

    public boolean isPresenceActive(UserPresence presence) {
        if (presence.getLastPing() == null) {
            return false;
        }
        long secondsSinceLastPing = java.time.Duration.between(
                presence.getLastPing(),
                LocalDateTime.now()
        ).getSeconds();
        return secondsSinceLastPing <= PRESENCE_TTL_SECONDS;
    }
}
