package org.resourceserver.modules.room.repository;

import org.resourceserver.modules.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByLastActivityAtAfter(Instant lastActivityAt);
}
