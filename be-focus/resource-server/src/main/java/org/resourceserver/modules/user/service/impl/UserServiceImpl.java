package org.resourceserver.modules.user.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.resourceserver.modules.user.dto.ChangeAvatarRequest;
import org.resourceserver.modules.user.dto.UpdateProfileRequest;
import org.resourceserver.modules.user.dto.UserResponse;
import org.resourceserver.modules.user.entity.User;
import org.resourceserver.modules.user.repository.UserRepository;
import org.resourceserver.modules.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    @Override
    public UserResponse getMyProfile() {
        String userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl());
    }

    @Override
    public UserResponse updateProfile(UpdateProfileRequest request) {
        String userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getAvatarUrl());
    }

    @Override
    public UserResponse changeAvatar(ChangeAvatarRequest request) {
        String userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    request.getAvatar().getBytes(),
                    ObjectUtils.asMap("folder", "users/avatar")
            );
            user.setAvatarUrl(result.get("secure_url").toString());
            User savedUser = userRepository.save(user);
            return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getAvatarUrl());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar");
        }
    }

    @Override
    public UserResponse testCloud(ChangeAvatarRequest request) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    request.getAvatar().getBytes(),
                    ObjectUtils.asMap("folder", "users/avatar")
            );
            System.out.println(result.get("secure_url").toString());
            System.out.println(result.get("public_id").toString());
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }
}
