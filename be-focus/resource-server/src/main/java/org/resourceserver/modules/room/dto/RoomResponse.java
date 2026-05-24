package org.resourceserver.modules.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private Long id;
    private String name;
    private String hostUserId;
    private String hostUsername;
    private String visibility;
    private String roomType;
    private int defaultDurationSeconds;
    private int maxParticipants;
    private int currentParticipants;
    private Instant createdAt;
    private Instant updatedAt;
    private List<RoomMemberResponse> members;
    private SessionInfoResponse currentSession;
}
