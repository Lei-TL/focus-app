package org.resourceserver.modules.room.repository;

import org.resourceserver.modules.room.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    List<RoomMember> findByRoomId(Long roomId);
    List<RoomMember> findByRoomIdOrderByJoinedAtAsc(Long roomId);
    Optional<RoomMember> findByRoomIdAndUserId(Long roomId, String userId);
    Optional<RoomMember> findByUserId(String userId);
    long countByRoomId(Long roomId);
    void deleteByRoomIdAndUserId(Long roomId, String userId);
}
