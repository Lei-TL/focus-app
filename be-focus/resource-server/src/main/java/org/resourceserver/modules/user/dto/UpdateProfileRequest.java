package org.resourceserver.modules.user.dto;


import lombok.Builder;
import lombok.Data;

import java.security.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
public class UpdateProfileRequest {

    private String id;
    private String username;
    private LocalDateTime dayOfBirth;
    private Timestamp timestamp;

}
