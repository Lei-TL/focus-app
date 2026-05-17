package org.resourceserver.modules.user.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.resourceserver.modules.user.dto.ChangeAvatarRequest;
import org.resourceserver.modules.user.dto.CreateUserRequest;
import org.resourceserver.modules.user.dto.UpdateProfileRequest;
import org.resourceserver.modules.user.dto.UserResponse;
import org.resourceserver.modules.user.entity.User;
import org.resourceserver.modules.user.mapper.UserMapper;
import org.resourceserver.modules.user.repository.UserRepository;
import org.resourceserver.modules.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final UserMapper userMapper;

    @Override
    public UserResponse getMyProfile() {
        return null;
    }

    @Override
    public UserResponse updateProfile(UpdateProfileRequest request) {
        return null;
    }

    @Override
    public UserResponse changeAvatar(ChangeAvatarRequest request) {
        return null;
    }

    @Override
    public UserResponse testCloud(ChangeAvatarRequest request) {
        try{

            Map <?,?> result = cloudinary.uploader().upload(
                    request.getAvatar().getBytes(),
                    ObjectUtils.asMap(
                            "folder","users/avatar"
                    )
            );

            System.out.println(result.get("secure_url").toString());
            System.out.println(result.get("public_id").toString());

        }
        catch (Exception e){
            return null;
        }
        return null;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();
        user.setAccountId(request.getAccountId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

}
