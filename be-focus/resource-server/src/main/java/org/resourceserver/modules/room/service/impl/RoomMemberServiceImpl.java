package org.resourceserver.modules.room.service.impl;

import lombok.RequiredArgsConstructor;
import org.resourceserver.modules.room.entity.RoomMember;
import org.resourceserver.modules.room.repository.RoomMemberRepository;
import org.resourceserver.modules.room.service.RoomMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomMemberServiceImpl implements RoomMemberService {

    private final RoomMemberRepository roomMemberRepository;

    @Override
    @Transactional
    public RoomMember addMember(Long roomId, String userId, RoomMember.Role role) {
        Optional<RoomMember> existingMember = roomMemberRepository.findByRoomIdAndUserId(roomId, userId);
        if (existingMember.isPresent()) {
            return existingMember.get();
        }
        RoomMember member = RoomMember.builder()
                .roomId(roomId)
                .userId(userId)
                .role(role)
                .build();
        return roomMemberRepository.save(member);
    }

    @Override
    public Optional<RoomMember> getMember(Long roomId, String userId) {
        return roomMemberRepository.findByRoomIdAndUserId(roomId, userId);
    }

    @Override
    public Optional<RoomMember> getMemberByUserId(String userId) {
        return roomMemberRepository.findByUserId(userId);
    }

    @Override
    public List<RoomMember> getMembersByRoomId(Long roomId) {
        return roomMemberRepository.findByRoomId(roomId);
    }

    @Override
    public long countMembersByRoomId(Long roomId) {
        return roomMemberRepository.countByRoomId(roomId);
    }

    @Override
    @Transactional
    public void removeMember(Long roomId, String userId) {
        roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
    }
}
