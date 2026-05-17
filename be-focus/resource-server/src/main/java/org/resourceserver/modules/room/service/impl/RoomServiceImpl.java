package org.resourceserver.modules.room.service.impl;

import lombok.RequiredArgsConstructor;
import org.resourceserver.common.response.AppEvent;
import org.resourceserver.modules.room.dto.CreateRoomRequest;
import org.resourceserver.modules.room.dto.RoomResponse;
import org.resourceserver.modules.room.entity.Participant;
import org.resourceserver.modules.room.entity.ParticipantStatus;
import org.resourceserver.modules.room.entity.Room;
import org.resourceserver.modules.room.entity.RoomStatus;
import org.resourceserver.modules.room.mapper.RoomMapper;
import org.resourceserver.modules.room.repository.RoomRepository;
import org.resourceserver.modules.room.service.RoomService;
import org.resourceserver.modules.session.entity.Session;
import org.resourceserver.modules.session.repository.SessionRepository;
import org.resourceserver.realtime.websocket.handler.RoomWebSocketHandler;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final SessionRepository sessionRepository;
    private final RoomWebSocketHandler webSocketHandler;
    private final Set<Long> activeRoomIds = Collections.synchronizedSet(new HashSet<>());

    @EventListener(ApplicationReadyEvent.class)
    public void initActiveRooms() {
        roomRepository.findByStatus(RoomStatus.FOCUSING)
                .forEach(room -> activeRoomIds.add(room.getId()));
    }

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        Room room = Room.builder()
                .name(request.getName())
                .defaultDuration(request.getDuration())
                .maxParticipants(request.getMaxParticipants())
                .isPublic(request.isPublic())
                .currentParticipants(0)
                .status(RoomStatus.WAITING)
                .participants(new ArrayList<>())
                .build();

        room = roomRepository.save(room);
        return RoomMapper.mapToResponse(room);
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse getRoomDetail(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return RoomMapper.mapToResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse joinRoom(Long roomId, String userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Optional<Participant> existing = room.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst();

        if (existing.isPresent()) {
            Participant p = existing.get();
            if (p.getStatus() == ParticipantStatus.LEFT) {
                p.setStatus(room.getStatus() == RoomStatus.FOCUSING ?
                        ParticipantStatus.FOCUSING : ParticipantStatus.WAITING);
            }
        } else {
            if (room.getCurrentParticipants() >= room.getMaxParticipants()) {
                throw new RuntimeException("Room is full");
            }

            ParticipantStatus initialStatus = (room.getStatus() == RoomStatus.FOCUSING) ?
                    ParticipantStatus.FOCUSING : ParticipantStatus.WAITING;

            Participant participant = Participant.builder()
                    .userId(userId)
                    .sessionId(room.getCurrentSession() != null ? room.getCurrentSession().getId() : null)
                    .status(initialStatus)
                    .joinTime(LocalDateTime.now())
                    .room(room)
                    .build();

            room.getParticipants().add(participant);
        }

        updateActiveCount(room);
        roomRepository.save(room);

        RoomResponse response = RoomMapper.mapToResponse(room);
        webSocketHandler.broadcastToRoom(roomId, AppEvent.USER_JOINED, response);

        return response;
    }

    @Override
    @Transactional
    public RoomResponse startSession(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStatus() == RoomStatus.FOCUSING) {
            throw new RuntimeException("Session already in progress");
        }

        Session session = Session.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .durationMinutes(room.getDefaultDuration())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(room.getDefaultDuration()))
                .status(Session.SessionStatus.FOCUSING)
                .build();

        room.setCurrentSession(session);
        room.setStatus(RoomStatus.FOCUSING);

        room.getParticipants().stream()
                .filter(p -> p.getStatus() == ParticipantStatus.WAITING)
                .forEach(p -> {
                    p.setStatus(ParticipantStatus.FOCUSING);
                    p.setSessionId(session.getId());
                });

        activeRoomIds.add(roomId);
        roomRepository.save(room);

        RoomResponse response = RoomMapper.mapToResponse(room);
        webSocketHandler.broadcastToRoom(roomId, AppEvent.SESSION_STARTED, response);

        return response;
    }

    @Override
    @Transactional
    public RoomResponse leaveRoom(Long roomId, String userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .ifPresent(p -> {
                    p.setLeaveTime(LocalDateTime.now());
                    p.setStatus(ParticipantStatus.LEFT);

                    if (room.getStatus() == RoomStatus.FOCUSING && room.getCurrentSession() != null) {
                        p.setStatus(ParticipantStatus.LEFT);
                        p.setLeaveTime(LocalDateTime.now());
                    }
                });

        updateActiveCount(room);

        if (room.getCurrentParticipants() == 0 && room.getStatus() == RoomStatus.FOCUSING) {
            room.setStatus(RoomStatus.ENDED);
            activeRoomIds.remove(roomId);
            if (room.getCurrentSession() != null) {
                room.getCurrentSession().setStatus(Session.SessionStatus.COMPLETED);
            }
        }

        roomRepository.save(room);

        RoomResponse response = RoomMapper.mapToResponse(room);
        webSocketHandler.broadcastToRoom(roomId, AppEvent.USER_LEFT, response);

        return response;
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void tickSessions() {
        if (activeRoomIds.isEmpty()) {
            return;
        }

        List<Room> activeRooms = roomRepository.findByStatus(RoomStatus.FOCUSING);
        if (activeRooms.isEmpty()) {
            activeRoomIds.clear();
            return;
        }

        activeRooms.forEach(room -> {
            if (room.getCurrentSession() != null) {
                Session session = room.getCurrentSession();

                if (LocalDateTime.now().isAfter(session.getEndTime())) {
                    session.setStatus(Session.SessionStatus.COMPLETED);
                    room.setStatus(RoomStatus.WAITING);
                    activeRoomIds.remove(room.getId());

                    LocalDateTime endTime = LocalDateTime.now();
                    room.getParticipants().stream()
                            .filter(p -> p.getStatus() == ParticipantStatus.FOCUSING)
                            .forEach(p -> {
                                p.setStatus(ParticipantStatus.COMPLETED);
                                p.setCompleted(true);
                                p.setLeaveTime(endTime);
                            });
                    webSocketHandler.broadcastToRoom(room.getId(), AppEvent.SESSION_COMPLETED, RoomMapper.mapToResponse(room));
                } else {
                    webSocketHandler.broadcastToRoom(room.getId(), AppEvent.SESSION_TICK, RoomMapper.mapToResponse(room));
                }
                roomRepository.save(room);
            }
        });
    }

    private void updateActiveCount(Room room) {
        long activeCount = room.getParticipants().stream()
                .filter(p -> p.getStatus() != ParticipantStatus.LEFT)
                .count();
        room.setCurrentParticipants((int) activeCount);
    }
}
