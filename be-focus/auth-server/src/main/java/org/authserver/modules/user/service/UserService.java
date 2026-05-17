package org.authserver.modules.user.service;

import org.authserver.modules.user.dto.ChangeAvatarRequest;
import org.authserver.modules.user.dto.SignupRequest;
import org.authserver.modules.user.dto.UserResponse;

public interface UserService {
    UserResponse signup(SignupRequest request);
    UserResponse getUserById(String userId);
    UserResponse getCurrentUser();
    UserResponse changeAvatar(ChangeAvatarRequest request);
}
