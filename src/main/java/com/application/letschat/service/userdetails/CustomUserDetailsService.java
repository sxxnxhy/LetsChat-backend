package com.application.letschat.service.userdetails;

import com.application.letschat.model.user.User;
import com.application.letschat.repository.user.UserRepository;
import com.application.letschat.repository.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByName(username);
        System.out.println(user);
        return CustomUserDetails.builder().user(user).build();
    }
}
