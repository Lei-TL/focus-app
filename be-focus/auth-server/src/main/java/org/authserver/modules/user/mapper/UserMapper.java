package org.authserver.modules.user.mapper;

import org.authserver.modules.user.dto.UserResponse;
import org.authserver.modules.user.entity.Account;

public class UserMapper {
    public static UserResponse mapToResponse(Account account) {
        return UserResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .avatarUrl(account.getAvatarUrl())
                .build();
    }
}
