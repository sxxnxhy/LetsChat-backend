package com.application.letschat.service.chatRoomUser;


import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.chatRoomUser.ChatRoomUser;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.chatRoomUser.ChatRoomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomUserService {

    private final ChatRoomUserRepository chatRoomUserRepository;
    private final JwtUtil jwtUtil;

    public void addUserToChatRoom(User user, ChatRoom chatRoom) {
        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setUser(user);
        chatRoomUser.setChatRoom(chatRoom);
        chatRoomUserRepository.save(chatRoomUser);
    }

    public boolean isUserInChat(Long chatRoomId, Integer userId) {
        boolean isValid = false;

        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new RuntimeException("No users found in chat room"));
        isValid = chatRoomUsers.stream()
                .anyMatch(chatRoomUser -> chatRoomUser.getUser().getUserId().equals(userId));
        return isValid;
    }


}
