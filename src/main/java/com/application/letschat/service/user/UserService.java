package com.application.letschat.service.user;

import com.application.letschat.dto.user.UserDTO;
import com.application.letschat.entity.user.User;
import com.application.letschat.repository.chatroom.ChatRoomRepository;
import com.application.letschat.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public boolean authenticate(UserDTO userDTO) {
        boolean authenticated = false;

        User user = getUserByEmail(userDTO.getEmail());
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserId(), userDTO.getPassword())
            );
            authenticated = true;
        } catch (AuthenticationException e) {
            log.info("{} 로그인 시도: {}", user.getName(), e.getMessage());
        }
        return authenticated;
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

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

//    public Page<UserDocument> getUsersByKeyword2(String keyword, Integer page) {
//        Pageable pageable = PageRequest.of(page, 2);
//        return userRepository.findByNameContaining(keyword, pageable);
//    }

//    public void addUser(UserDocument user) {
//        userRepository.save(user);
//    }

//    public List<UserDocument> getUserList() {
//        List<UserDocument> userList = new ArrayList<>();
//        userRepository.findAll().forEach(userList::add);  // Adds all elements to the List
//        for (UserDocument user : userList) {
//            System.out.println(user);
//        }
//        return userList;
//    }

}