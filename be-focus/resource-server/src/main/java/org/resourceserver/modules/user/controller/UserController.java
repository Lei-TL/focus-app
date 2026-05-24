package org.resourceserver.modules.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resourceserver.common.response.ApiResponse;
import org.resourceserver.config.JwtTokenProvider;
import org.resourceserver.modules.user.dto.*;
import org.resourceserver.modules.user.entity.User;
import org.resourceserver.modules.user.repository.UserRepository;
import org.resourceserver.modules.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping ("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/auth/signup")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received: email={}, username={}", request.getEmail(), request.getUsername());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Signup failed: email already exists - {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User created successfully: id={}", savedUser.getId());
        String token = tokenProvider.generateToken(savedUser.getEmail(), savedUser.getId());

        UserResponse userResponse = new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getAvatarUrl()
        );

        return ApiResponse.success(new AuthResponse(token, userResponse));
    }

    @PostMapping("/auth/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received: email={}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid credentials for email={}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getId());

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl()
        );

        return ApiResponse.success(new AuthResponse(token, userResponse));
    }

    @GetMapping("/users/me")
    public ApiResponse<UserResponse> getMyProfile(){
        UserResponse user = userService.getMyProfile();
        return ApiResponse.success(user);
    }

    @PatchMapping("/users/me")
    public ApiResponse<UserResponse> updateProfile(@RequestBody UpdateProfileRequest request){
        UserResponse user = userService.updateProfile(request);
        return ApiResponse.success(user);
    }

    @PatchMapping("/users/me/avatar")
    public ApiResponse<UserResponse> changeAvatar(@RequestBody ChangeAvatarRequest request){
        return ApiResponse.success(userService.changeAvatar(request));
    }

    @PostMapping("/users/test-cloudinary")
    public ApiResponse<?> uploadImage(@RequestBody ChangeAvatarRequest request){
        return ApiResponse.success(userService.testCloud(request));
    }

}
