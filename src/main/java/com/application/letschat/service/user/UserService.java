package com.application.letschat.service.user;

import com.application.letschat.dto.user.*;
import com.application.letschat.entity.user.User;
import com.application.letschat.repository.chatroom.ChatRoomRepository;
import com.application.letschat.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatRoomRepository chatRoomRepository;
    private final AuthenticationManager authenticationManager;

    public List<User> getUsersByKeyword(String keyword) {
        return userRepository.findByKeyword(keyword);
    }

    public boolean isAuthenticated(LoginRequestDto loginRequestDto) {
        boolean authenticated = false;
        User user = getUserByEmail(loginRequestDto.getEmail());
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserId(), loginRequestDto.getPassword())
            );
            authenticated = true;
        } catch (AuthenticationException e) {
            log.info("{} 로그인 시도: {}", user.getName(), e.getMessage());
        }
        return authenticated;
    }

    public User createUser(SignUpRequestDto signUpRequestDto) {
        User user = new User();
        user.setName(signUpRequestDto.getName());
        user.setEmail(signUpRequestDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        return userRepository.save(user);
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    public List<Integer> findUserIdsByChatRoomId(Long chatRoomId) {
        return new ArrayList<>(chatRoomRepository.findUserIdsByChatRoomId(chatRoomId));
    }

    public List<User> getUsersById(List<Integer> usersInChatRoom) {
        return userRepository.findAllById(usersInChatRoom); // Fetch users from DB
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserInfoDto getUserInfoByEmail(String email) {
        User user = getUserByEmail(email);
        return UserInfoDto.builder()
                .name(user.getName())
                .userId(user.getUserId())
                .email(user.getEmail())
                .build();
    }
    @Transactional
    public void updateUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getUserId()).orElseThrow();
        user.setName(userDto.getName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
    }

    public UserInfoDto extractUserInfoFromSpringSecurity(Principal principal) {
//        //자바 17이상부터는 instanceof로 대체 가능
//        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
//        CustomUserDetails userDetails = (CustomUserDetails) authToken.getPrincipal();
//        String username = userDetails.getUsername();
//        Integer userId = Integer.parseInt(userDetails.getUserId());
        if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
            Object principalObj = authToken.getPrincipal();
            if (principalObj instanceof CustomUserDetails userDetails) {
                return UserInfoDto.builder().userId(Integer.parseInt(userDetails.getUserId())).name(userDetails.getUsername()).build();
            }
        }
        throw new IllegalArgumentException("Invalid principal");
    }
}