package org.authserver.modules.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.authserver.common.response.ApiResponse;
import org.authserver.modules.user.dto.ChangeAvatarRequest;
import org.authserver.modules.user.dto.SignupRequest;
import org.authserver.modules.user.dto.UserResponse;
import org.authserver.modules.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = userService.signup(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable String userId) {
        UserResponse response = userService.getUserById(userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ApiResponse.success(response);
    }

    @PutMapping("/avatar")
    public ApiResponse<UserResponse> changeAvatar(@Valid @RequestBody ChangeAvatarRequest request) {
        UserResponse response = userService.changeAvatar(request);
        return ApiResponse.success(response);
    }
}
