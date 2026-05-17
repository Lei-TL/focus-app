package org.resourceserver.modules.room.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ParticipantResponse {
    private String userId;
    private String status;
    private LocalDateTime joinTime;
    private boolean completed;
}
