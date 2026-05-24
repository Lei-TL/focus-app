package org.resourceserver.modules.room.service;

import org.resourceserver.modules.room.entity.RoomMember;

import java.util.List;
import java.util.Optional;

public interface RoomMemberService {
    RoomMember addMember(Long roomId, String userId, RoomMember.Role role);
    void removeMember(Long roomId, String userId);
    Optional<RoomMember> getMember(Long roomId, String userId);
    Optional<RoomMember> getMemberByUserId(String userId);
    List<RoomMember> getMembersByRoomId(Long roomId);
    long countMembersByRoomId(Long roomId);
}
