package com.application.letschat.service.chatRoomUser;


import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.chatRoomUser.ChatRoomUser;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.chatRoomUser.ChatRoomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomUserService {

    private final ChatRoomUserRepository chatRoomUserRepository;

    public void addUserToChatRoom(User user, ChatRoom chatRoom) {
        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setUser(user);
        chatRoomUser.setChatRoom(chatRoom);
        chatRoomUserRepository.save(chatRoomUser);
    }
}
