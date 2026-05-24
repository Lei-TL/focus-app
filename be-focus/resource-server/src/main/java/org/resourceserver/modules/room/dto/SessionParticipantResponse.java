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
public class SessionParticipantResponse {
    private Long id;
    private String sessionId;
    private String userId;
    private String username;
    private String avatarUrl;
    private String status;
    private Instant joinedAt;
    private Instant leftAt;
    private Instant completedAt;
    private int focusDurationSeconds;
    private double completionRate;
    private boolean completed;
}
