// package org.resourceserver.modules.room.scheduler;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.resourceserver.modules.room.entity.Room;
// import org.resourceserver.modules.room.repository.RoomRepository;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.Duration;
// import java.time.Instant;
// import java.util.List;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class RoomScheduler {

//     private final RoomRepository roomRepository;

//     @Scheduled(fixedRate = 60000)
//     @Transactional
//     public void archiveInactiveRooms() {
//         Instant tenMinutesAgo = Instant.now().minus(Duration.ofMinutes(10));
//         List<Room> inactiveRooms = roomRepository.findByArchivedFalseAndCurrentSessionIdIsNullAndLastActivityAtBefore(tenMinutesAgo);

//         for (Room room : inactiveRooms) {
//             log.info("Archiving inactive room: {}", room.getId());
//             room.setArchived(true);
//             roomRepository.save(room);
//         }
//     }
// }
