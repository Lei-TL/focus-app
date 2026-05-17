package org.authserver.modules.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.authserver.modules.user.dto.ChangeAvatarRequest;
import org.authserver.modules.user.dto.SignupRequest;
import org.authserver.modules.user.dto.UserResponse;
import org.authserver.modules.user.entity.Account;
import org.authserver.modules.user.mapper.UserMapper;
import org.authserver.modules.user.repository.AccountRepository;
import org.authserver.modules.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        account = accountRepository.save(account);
        return UserMapper.mapToResponse(account);
    }

    @Override
    public UserResponse getUserById(String userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return UserMapper.mapToResponse(account);
    }

    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return UserMapper.mapToResponse(account);
    }

    @Override
    @Transactional
    public UserResponse changeAvatar(ChangeAvatarRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.setAvatarUrl(request.getAvatarUrl());
        account = accountRepository.save(account);
        return UserMapper.mapToResponse(account);
    }
}
