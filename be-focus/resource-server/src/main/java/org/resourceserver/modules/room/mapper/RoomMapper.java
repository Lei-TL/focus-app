package org.resourceserver.modules.room.mapper;

import lombok.RequiredArgsConstructor;
import org.resourceserver.modules.room.dto.RoomMemberResponse;
import org.resourceserver.modules.room.dto.RoomResponse;
import org.resourceserver.modules.room.dto.SessionInfoResponse;
import org.resourceserver.modules.room.entity.Room;
import org.resourceserver.modules.room.entity.RoomMember;
import org.resourceserver.modules.room.repository.RoomMemberRepository;
import org.resourceserver.modules.session.entity.FocusSession;
import org.resourceserver.modules.session.entity.SessionParticipant;
import org.resourceserver.modules.session.repository.FocusSessionRepository;
import org.resourceserver.modules.session.repository.SessionParticipantRepository;
import org.resourceserver.modules.user.entity.User;
import org.resourceserver.modules.user.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    private final RoomMemberRepository roomMemberRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final UserRepository userRepository;
    private final RoomMemberMapper roomMemberMapper;
    private final FocusSessionMapper focusSessionMapper;

    public RoomResponse toRoomResponse(Room room) {
        List<RoomMember> roomMembers = roomMemberRepository.findByRoomId(room.getId());
        Map<String, User> userMap = userRepository.findAllById(
                roomMembers.stream().map(RoomMember::getUserId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<RoomMemberResponse> memberResponses = roomMembers.stream()
                .map(member -> roomMemberMapper.toRoomMemberResponse(member, userMap.get(member.getUserId())))
                .collect(Collectors.toList());

        SessionInfoResponse currentSessionResponse = null;
        FocusSession activeSession = null;
        if (room.getCurrentSessionId() != null) {
            activeSession = focusSessionRepository.findById(room.getCurrentSessionId()).orElse(null);
        }

        if (activeSession != null && activeSession.getStatus() == FocusSession.SessionStatus.FOCUSING) {
            List<SessionParticipant> sessionParticipants = sessionParticipantRepository
                    .findBySessionId(activeSession.getId());
            Map<String, User> sessionUserMap = userRepository.findAllById(
                    sessionParticipants.stream().map(SessionParticipant::getUserId).collect(Collectors.toList())
            ).stream().collect(Collectors.toMap(User::getId, u -> u));

            currentSessionResponse = focusSessionMapper.toSessionInfoResponse(
                    activeSession,
                    sessionParticipants,
                    sessionUserMap
            );
        }

        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .hostUserId(room.getHostUserId())
                .hostUsername(userMap.get(room.getHostUserId()) != null ?
                        userMap.get(room.getHostUserId()).getUsername() : "Unknown User")
                .visibility(room.getVisibility().name())
                .roomType(room.getRoomType().name())
                .defaultDurationSeconds(room.getDefaultDurationSeconds())
                .maxParticipants(room.getMaxParticipants())
                .currentParticipants(memberResponses.size())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .members(memberResponses)
                .currentSession(currentSessionResponse)
                .build();
    }
}
