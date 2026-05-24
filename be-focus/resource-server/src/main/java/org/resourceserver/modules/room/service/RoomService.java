package org.resourceserver.modules.room.service;

import org.resourceserver.modules.room.dto.CreateRoomRequest;
import org.resourceserver.modules.room.dto.RoomResponse;
import org.resourceserver.modules.room.dto.UpdateRoomRequest;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom(CreateRoomRequest request, String userId);
    List<RoomResponse> getAllRooms();
    RoomResponse getRoomDetail(Long roomId);
    RoomResponse joinRoom(Long roomId, String userId);
    RoomResponse leaveRoom(Long roomId, String userId);
    RoomResponse startSession(Long roomId, String userId);
    RoomResponse endSession(Long roomId, String userId);
    RoomResponse updateRoom(Long roomId, UpdateRoomRequest request, String userId);
}
