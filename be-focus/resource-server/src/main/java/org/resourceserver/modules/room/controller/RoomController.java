package org.resourceserver.modules.room.controller;

import lombok.RequiredArgsConstructor;
import org.resourceserver.common.response.ApiResponse;
import org.resourceserver.modules.room.dto.CreateRoomRequest;
import org.resourceserver.modules.room.dto.RoomResponse;
import org.resourceserver.modules.room.dto.UpdateRoomRequest;
import org.resourceserver.modules.room.service.RoomService;
import org.resourceserver.modules.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@RequestBody CreateRoomRequest request) {
        String userId = userService.getCurrentUserId();
        RoomResponse response = roomService.createRoom(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomDetail(@PathVariable Long roomId) {
        RoomResponse room = roomService.getRoomDetail(roomId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(@PathVariable Long roomId) {
        String userId = userService.getCurrentUserId();
        RoomResponse response = roomService.joinRoom(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{roomId}/start")
    public ResponseEntity<ApiResponse<RoomResponse>> startSession(@PathVariable Long roomId) {
        String userId = userService.getCurrentUserId();
        RoomResponse response = roomService.startSession(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<RoomResponse>> leaveRoom(@PathVariable Long roomId) {
        String userId = userService.getCurrentUserId();
        RoomResponse response = roomService.leaveRoom(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{roomId}/end")
    public ResponseEntity<ApiResponse<RoomResponse>> endSession(@PathVariable Long roomId) {
        String userId = userService.getCurrentUserId();
        RoomResponse response = roomService.endSession(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(@PathVariable Long roomId, @RequestBody UpdateRoomRequest request) {
        String userId = userService.getCurrentUserId();
        RoomResponse response = roomService.updateRoom(roomId, request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
