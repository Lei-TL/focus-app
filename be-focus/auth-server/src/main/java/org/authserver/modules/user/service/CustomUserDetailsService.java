package org.authserver.modules.user.service;

import lombok.RequiredArgsConstructor;
import org.authserver.modules.user.entity.Account;
import org.authserver.modules.user.repository.AccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username)
                .orElseGet(() -> accountRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Account not found with username or email: " + username)));

        return new org.springframework.security.core.userdetails.User(
                account.getEmail(),
                account.getPassword(),
                new ArrayList<>()
        );
    }
}
