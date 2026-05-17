package org.resourceserver.modules.room.mapper;

import org.resourceserver.modules.room.dto.ParticipantResponse;
import org.resourceserver.modules.room.dto.RoomResponse;
import org.resourceserver.modules.room.entity.Participant;
import org.resourceserver.modules.room.entity.ParticipantStatus;
import org.resourceserver.modules.room.entity.Room;
import org.resourceserver.modules.session.entity.Session;

import java.util.List;
import java.util.stream.Collectors;

public class RoomMapper {

    public static RoomResponse mapToResponse(Room room) {
        RoomResponse.SessionInfo sessionInfo = null;
        if (room.getCurrentSession() != null) {
            Session session = room.getCurrentSession();
            sessionInfo = RoomResponse.SessionInfo.builder()
                    .id(session.getId())
                    .remainingSeconds(session.calculateRemainingSeconds())
                    .status(session.getStatus().name())
                    .build();
        }

        List<ParticipantResponse> participantResponses = room.getParticipants().stream()
                .map(p -> ParticipantResponse.builder()
                        .userId(p.getUserId())
                        .status(p.getStatus().name())
                        .joinTime(p.getJoinTime())
                        .completed(p.isCompleted())
                        .build())
                .collect(Collectors.toList());

        RoomResponse.ParticipantStats stats = RoomResponse.ParticipantStats.builder()
                .focusing(room.getParticipants().stream().filter(p -> p.getStatus() == ParticipantStatus.FOCUSING).count())
                .completed(room.getParticipants().stream().filter(p -> p.isCompleted()).count())
                .leftEarly(room.getParticipants().stream().filter(p -> p.getStatus() == ParticipantStatus.LEFT).count())
                .build();

        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .duration(room.getDefaultDuration())
                .maxParticipants(room.getMaxParticipants())
                .currentParticipants(room.getCurrentParticipants())
                .isPublic(room.isPublic())
                .status(room.getStatus())
                .session(sessionInfo)
                .participants(participantResponses)
                .stats(stats)
                .build();
    }
}
