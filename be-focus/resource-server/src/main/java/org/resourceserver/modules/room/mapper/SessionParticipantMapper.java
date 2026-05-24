package org.resourceserver.modules.room.mapper;

import org.resourceserver.modules.room.dto.SessionParticipantResponse;
import org.resourceserver.modules.session.entity.SessionParticipant;
import org.resourceserver.modules.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class SessionParticipantMapper {

    public SessionParticipantResponse toSessionParticipantResponse(
            SessionParticipant participant,
            User user
    ) {
        return SessionParticipantResponse.builder()
                .id(participant.getId())
                .sessionId(participant.getSessionId())
                .userId(participant.getUserId())
                .username(user != null ? user.getUsername() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .status(participant.getStatus().name())
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .completedAt(participant.getCompletedAt())
                .focusDurationSeconds(participant.getFocusDurationSeconds())
                .completionRate(participant.getCompletionRate())
                .completed(participant.isCompleted())
                .build();
    }
}
