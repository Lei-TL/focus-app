package org.resourceserver.modules.room.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoomRequest {
    @NotBlank(message = "Room name is required")
    private String name;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private int duration;

    private int maxParticipants = 20;

    private boolean isPublic = true;
}
