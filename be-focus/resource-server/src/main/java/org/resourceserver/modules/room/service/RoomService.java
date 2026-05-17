package org.resourceserver.modules.room.service;

import org.resourceserver.modules.room.dto.CreateRoomRequest;
import org.resourceserver.modules.room.dto.RoomResponse;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom(CreateRoomRequest request);
    List<RoomResponse> getAllRooms();
    RoomResponse getRoomDetail(Long roomId);
    RoomResponse joinRoom(Long roomId, String userId);
    RoomResponse startSession(Long roomId);
    RoomResponse leaveRoom(Long roomId, String userId);
}
