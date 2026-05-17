package org.resourceserver.modules.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SoloSessionRequest {
    private String userId;
    private int durationSeconds;
    private boolean completed;
}
