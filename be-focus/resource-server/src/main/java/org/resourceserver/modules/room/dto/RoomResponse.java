package org.resourceserver.modules.room.dto;

import lombok.Builder;
import lombok.Data;
import org.resourceserver.modules.room.entity.RoomStatus;

import java.util.List;

@Data
@Builder
public class RoomResponse {
    private Long id;
    private String name;
    private int maxParticipants;
    private int currentParticipants;
    private boolean isPublic;
    private RoomStatus status;
    private int duration;
    private SessionInfo session;
    private List<ParticipantResponse> participants;
    private ParticipantStats stats;

    @Data
    @Builder
    public static class SessionInfo {
        private String id;
        private long remainingSeconds;
        private String status;
    }

    @Data
    @Builder
    public static class ParticipantStats {
        private long focusing;
        private long completed;
        private long leftEarly;
    }
}
