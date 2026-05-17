package org.resourceserver.modules.user.service;

import org.resourceserver.modules.user.dto.ChangeAvatarRequest;
import org.resourceserver.modules.user.dto.CreateUserRequest;
import org.resourceserver.modules.user.dto.UpdateProfileRequest;
import org.resourceserver.modules.user.dto.UserResponse;

public interface UserService {

    public UserResponse getMyProfile();
    public UserResponse updateProfile(UpdateProfileRequest request);
    public UserResponse changeAvatar(ChangeAvatarRequest request);
    public UserResponse testCloud(ChangeAvatarRequest request);
    public UserResponse createUser(CreateUserRequest request);

}
