package org.resourceserver.modules.room.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.resourceserver.common.response.ApiResponse;
import org.resourceserver.modules.room.dto.CreateRoomRequest;
import org.resourceserver.modules.room.dto.RoomResponse;
import org.resourceserver.modules.room.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ApiResponse.success(roomService.createRoom(request));
    }

    @GetMapping
    public ApiResponse<List<RoomResponse>> getAllRooms() {
        return ApiResponse.success(roomService.getAllRooms());
    }

    @GetMapping("/{roomId}")
    public ApiResponse<RoomResponse> getRoomDetail(@PathVariable Long roomId) {
        return ApiResponse.success(roomService.getRoomDetail(roomId));
    }

    @PostMapping("/{roomId}/join")
    public ApiResponse<RoomResponse> joinRoom(@PathVariable Long roomId, @RequestParam String userId) {
        return ApiResponse.success(roomService.joinRoom(roomId, userId));
    }

    @PostMapping("/{roomId}/start")
    public ApiResponse<RoomResponse> startSession(@PathVariable Long roomId) {
        return ApiResponse.success(roomService.startSession(roomId));
    }

    @PostMapping("/{roomId}/leave")
    public ApiResponse<RoomResponse> leaveRoom(@PathVariable Long roomId, @RequestParam String userId) {
        return ApiResponse.success(roomService.leaveRoom(roomId, userId));
    }
}
