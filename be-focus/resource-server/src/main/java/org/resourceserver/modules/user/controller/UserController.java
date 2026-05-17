package org.resourceserver.modules.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.resourceserver.common.response.ApiResponse;
import org.resourceserver.modules.user.dto.ChangeAvatarRequest;
import org.resourceserver.modules.user.dto.CreateUserRequest;
import org.resourceserver.modules.user.dto.UpdateProfileRequest;
import org.resourceserver.modules.user.dto.UserResponse;
import org.resourceserver.modules.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/public/register")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request){
        UserResponse user = userService.createUser(request);
        return ApiResponse.success(user);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile(){
        UserResponse user = userService.getMyProfile();
        return ApiResponse.success(user);
    }

    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@RequestBody UpdateProfileRequest request){
        UserResponse user = userService.updateProfile(request);
        return ApiResponse.success(user);
    }

    @PatchMapping("/me/avatar")
    public ApiResponse<UserResponse> changeAvatar(@RequestBody ChangeAvatarRequest request){
        return ApiResponse.success(userService.changeAvatar(request));
    }

    @PostMapping("/test-cloudinary")
    public ApiResponse<?> uploadImage(@RequestBody ChangeAvatarRequest request){
        return ApiResponse.success(userService.testCloud(request));
    }

}
