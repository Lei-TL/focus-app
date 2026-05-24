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
public class SessionInfoResponse {
    private String id;
    private Long roomId;
    private String status;
    private String startedBy;
    private Instant startTime;
    private Instant endTime;
    private Instant actualEndTime;
    private int durationSeconds;
    private int remainingSeconds;
    private List<SessionParticipantResponse> participants;
}
