package org.resourceserver.modules.room.mapper;

import org.resourceserver.modules.room.dto.RoomMemberResponse;
import org.resourceserver.modules.room.entity.RoomMember;
import org.resourceserver.modules.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class RoomMemberMapper {

    public RoomMemberResponse toRoomMemberResponse(RoomMember member, User user) {
        return RoomMemberResponse.builder()
                .id(member.getId())
                .roomId(member.getRoomId())
                .userId(member.getUserId())
                .username(user != null ? user.getUsername() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .lastSeenAt(member.getLastSeenAt())
                .build();
    }
}
