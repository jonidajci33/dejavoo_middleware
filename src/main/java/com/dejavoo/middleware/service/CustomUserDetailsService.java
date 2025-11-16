package com.dejavoo.middleware.service;

import com.dejavoo.middleware.entity.MerchantUser;
import com.dejavoo.middleware.repository.MerchantUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final MerchantUserRepository merchantUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);

        MerchantUser merchantUser = merchantUserRepository.findByUsernameAndActive(username, true)
                .orElseThrow(() -> {
                    log.warn("User not found or inactive: {}", username);
                    return new UsernameNotFoundException("User not found or inactive: " + username);
                });

        log.debug("User found: {}", username);

        // Create authorities (roles)
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // Return Spring Security User with the password from database
        return User.builder()
                .username(merchantUser.getUsername())
                .password(merchantUser.getPassword()) // Password should be BCrypt encoded in database
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!merchantUser.getActive())
                .build();
    }
}
