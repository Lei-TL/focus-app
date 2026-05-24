package org.resourceserver.modules.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoomRequest {
    private Integer defaultDurationSeconds;
    private Integer maxParticipants;
}
