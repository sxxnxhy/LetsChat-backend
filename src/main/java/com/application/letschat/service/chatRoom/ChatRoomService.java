package com.application.letschat.service.chatRoom;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatRoom.ChatRoomDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.chatRoomUser.ChatRoomUser;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.chatRoom.ChatRoomRepository;
import com.application.letschat.repository.chatRoomUser.ChatRoomUserRepository;
import com.application.letschat.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomUserRepository chatRoomUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;


    public Long createChatRoom(Integer userId, ChatRoomDTO chatRoomDTO, Integer targetUserId) {

        ChatRoom chatRoom = new ChatRoom();
        User user = userRepository.findById(userId).orElseThrow();
        chatRoom.setChatRoomName(user.getName() + ", " + chatRoomDTO.getChatRoomName());
        ChatRoom result = chatRoomRepository.save(chatRoom);

        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setUser(user);
        chatRoomUser.setChatRoom(result);
        chatRoomUserRepository.save(chatRoomUser);

        ChatRoomUser chatRoomUser2 = new ChatRoomUser();
        User user2 = userRepository.findById(targetUserId).orElseThrow();
        chatRoomUser2.setUser(user2);
        chatRoomUser2.setChatRoom(result);
        chatRoomUserRepository.save(chatRoomUser2);


        return result.getChatRoomId();
    }


    public Boolean checkAccess(String token, Long chatRoomId) {
        boolean isValid = false;

        Integer userId = jwtUtil.getUserIdFromToken(token);

        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new RuntimeException("No users found in chat room"));

        isValid = chatRoomUsers.stream()
                .anyMatch(chatRoomUser -> chatRoomUser.getUser().getUserId().equals(userId));

        return isValid;
    }
}
