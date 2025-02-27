package com.application.letschat.service.userdetails;

import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // Your user repo

    @Override
    public CustomUserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Integer id = Integer.parseInt(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return CustomUserDetails.builder()
                .userId(user.getUserId().toString())
                .username(user.getName())
                .password(user.getPassword())
                .authorities(List.of())
                .build();

    }
}