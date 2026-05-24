package org.resourceserver.modules.room.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resourceserver.common.events.SessionStartedEvent;
import org.resourceserver.common.events.UserJoinedEvent;
import org.resourceserver.common.events.UserLeftEvent;
import org.resourceserver.common.response.AppEvent;
import org.resourceserver.modules.room.dto.CreateRoomRequest;
import org.resourceserver.modules.room.dto.RoomResponse;
import org.resourceserver.modules.room.dto.UpdateRoomRequest;
import org.resourceserver.modules.room.entity.Room;
import org.resourceserver.modules.room.entity.RoomMember;
import org.resourceserver.modules.room.mapper.RoomMapper;
import org.resourceserver.modules.room.repository.RoomMemberRepository;
import org.resourceserver.modules.room.repository.RoomRepository;
import org.resourceserver.modules.room.service.RoomMemberService;
import org.resourceserver.modules.room.service.RoomService;
import org.resourceserver.modules.session.entity.FocusSession;
import org.resourceserver.modules.session.entity.SessionParticipant;
import org.resourceserver.modules.session.service.FocusSessionService;
import org.resourceserver.modules.session.service.SessionParticipantService;
import org.resourceserver.realtime.websocket.handler.RoomWebSocketHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberService roomMemberService;
    private final RoomMemberRepository roomMemberRepository;
    private final FocusSessionService focusSessionService;
    private final SessionParticipantService sessionParticipantService;
    private final RoomMapper roomMapper;
    private final RoomWebSocketHandler webSocketHandler;
    private final ApplicationEventPublisher eventPublisher;

    private Room updateLastActivity(Room room) {
        room.setLastActivityAt(Instant.now());
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request, String userId) {
        Room.RoomType roomType = request.getRoomType() != null 
                ? Room.RoomType.valueOf(request.getRoomType().toUpperCase()) 
                : Room.RoomType.NORMAL;
                
        Room room = Room.builder()
                .name(request.getName())
                .hostUserId(userId)
                .visibility(Room.Visibility.valueOf(request.getVisibility().toUpperCase()))
                .roomType(roomType)
                .defaultDurationSeconds(request.getDefaultDurationSeconds())
                .maxParticipants(request.getMaxParticipants())
                .lastActivityAt(Instant.now())
                .build();

        room = roomRepository.save(room);
        roomMemberService.addMember(room.getId(), userId, RoomMember.Role.HOST);

        RoomResponse response = roomMapper.toRoomResponse(room);
        return response;
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        Instant tenMinutesAgo = Instant.now().minus(java.time.Duration.ofMinutes(10));
        return roomRepository.findByLastActivityAtAfter(tenMinutesAgo).stream()
                .map(roomMapper::toRoomResponse)
                .sorted((a, b) -> Integer.compare(b.getCurrentParticipants(), a.getCurrentParticipants()))
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse getRoomDetail(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        return roomMapper.toRoomResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse joinRoom(Long roomId, String userId) {
        log.info("joinRoom called for roomId={}, userId={}", roomId, userId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        long currentMembers = roomMemberService.countMembersByRoomId(roomId);
        if (currentMembers >= room.getMaxParticipants()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is full");
        }

        roomMemberService.getMember(roomId, userId).ifPresentOrElse(
            member -> {},
            () -> {
                log.info("User {} not in room {}, checking if in other room", userId, roomId);
                roomMemberService.getMemberByUserId(userId).ifPresent(existingMember -> {
                    Long existingRoomId = existingMember.getRoomId();
                    log.info("User {} was in room {}, checking if it's different from target room {}", userId, existingRoomId, roomId);
                    if (!existingRoomId.equals(roomId)) {
                        log.info("User {} was in different room {}, leaving that room", userId, existingRoomId);
                        leaveRoom(existingRoomId, userId);
                    }
                });
                log.info("Adding user {} to room {} as MEMBER", userId, roomId);
                roomMemberService.addMember(roomId, userId, RoomMember.Role.MEMBER);
            }
        );

        room = updateLastActivity(room);
        RoomResponse response = roomMapper.toRoomResponse(room);
        webSocketHandler.broadcastToRoom(roomId, AppEvent.USER_JOINED, response);
        eventPublisher.publishEvent(new UserJoinedEvent(this, roomId, userId));

        return response;
    }

    @Override
    @Transactional
    public RoomResponse leaveRoom(Long roomId, String userId) {
        log.info("leaveRoom called for roomId={}, userId={}", roomId, userId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (room.getCurrentSessionId() != null) {
            log.info("Marking user {} as LEFT_EARLY in session {}", userId, room.getCurrentSessionId());
            sessionParticipantService.getParticipant(room.getCurrentSessionId(), userId).ifPresent(participant -> {
                participant.setStatus(SessionParticipant.ParticipantStatus.LEFT_EARLY);
                participant.setLeftAt(Instant.now());
                if (participant.getJoinedAt() != null) {
                    long seconds = java.time.Duration.between(participant.getJoinedAt(), Instant.now()).getSeconds();
                    participant.setFocusDurationSeconds((int) seconds);
                }
            });
        }

        boolean isHost = room.getHostUserId().equals(userId);
        
        roomMemberService.removeMember(roomId, userId);

        if (isHost) {
            List<RoomMember> remainingMembers = roomMemberRepository.findByRoomIdOrderByJoinedAtAsc(roomId);
            if (!remainingMembers.isEmpty()) {
                RoomMember newHost = remainingMembers.get(0);
                newHost.setRole(RoomMember.Role.HOST);
                roomMemberRepository.save(newHost);
                room.setHostUserId(newHost.getUserId());
            }
        }

        room = updateLastActivity(room);
        RoomResponse response = roomMapper.toRoomResponse(room);
        
        if (isHost) {
            webSocketHandler.broadcastToRoom(roomId, AppEvent.HOST_CHANGED, response);
        }
        
        webSocketHandler.broadcastToRoom(roomId, AppEvent.USER_LEFT, response);
        eventPublisher.publishEvent(new UserLeftEvent(this, roomId, userId));

        return response;
    }

    @Override
    @Transactional
    public RoomResponse startSession(Long roomId, String userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (!room.getHostUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only host can start session");
        }

        if (focusSessionService.getActiveSession(roomId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session already in progress");
        }

        FocusSession session = focusSessionService.createSession(
                roomId,
                userId,
                room.getDefaultDurationSeconds()
        );

        if (room.getCurrentSessionId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room already has an active session");
        }

        List<RoomMember> members = roomMemberService.getMembersByRoomId(roomId);
        for (RoomMember member : members) {
            sessionParticipantService.addParticipant(session.getId(), member.getUserId());
        }

        room.setCurrentSessionId(session.getId());
        room = updateLastActivity(room);

        RoomResponse response = roomMapper.toRoomResponse(room);
        webSocketHandler.broadcastToRoom(roomId, AppEvent.SESSION_STARTED, response);
        eventPublisher.publishEvent(new SessionStartedEvent(this, roomId, session.getId()));

        return response;
    }

    @Override
    @Transactional
    public RoomResponse endSession(Long roomId, String userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (!room.getHostUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only host can end session");
        }

        if (room.getCurrentSessionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active session");
        }

        focusSessionService.endSession(room.getCurrentSessionId());

        room.setCurrentSessionId(null);
        room = updateLastActivity(room);
        RoomResponse response = roomMapper.toRoomResponse(room);
        webSocketHandler.broadcastToRoom(roomId, AppEvent.SESSION_COMPLETED, response);

        return response;
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long roomId, UpdateRoomRequest request, String userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (!room.getHostUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only host can update room");
        }

        if (room.getCurrentSessionId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update room while session is active");
        }

        if (request.getDefaultDurationSeconds() != null) {
            room.setDefaultDurationSeconds(request.getDefaultDurationSeconds());
        }

        if (request.getMaxParticipants() != null) {
            long currentMembers = roomMemberService.countMembersByRoomId(roomId);
            if (request.getMaxParticipants() < currentMembers) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max participants cannot be less than current number of participants");
            }
            room.setMaxParticipants(request.getMaxParticipants());
        }

        room = updateLastActivity(room);
        return roomMapper.toRoomResponse(room);
    }
}
