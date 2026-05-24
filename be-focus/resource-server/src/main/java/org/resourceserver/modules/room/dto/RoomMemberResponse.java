package org.resourceserver.modules.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomMemberResponse {
    private Long id;
    private Long roomId;
    private String userId;
    private String username;
    private String avatarUrl;
    private String role;
    private Instant joinedAt;
    private Instant lastSeenAt;
}
