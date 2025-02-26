package com.application.letschat.service.chatList;


import com.application.letschat.dto.chatList.ChatListDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.chatRoomUser.ChatRoomUser;
import com.application.letschat.model.message.Message;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.chatRoomUser.ChatRoomUserRepository;
import com.application.letschat.repository.message.MessageRepository;
import com.application.letschat.repository.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatListService {

    private final ChatRoomUserRepository chatRoomUserRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;


    public List<ChatListDTO> getChatList(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findByUser(user);

        return chatRoomUsers.stream()
                .map(cru -> {
                    ChatRoom chatRoom = cru.getChatRoom();
                    Message lastMessage = messageRepository.findLastMessageByChatRoom(chatRoom)
                            .orElse(null);
                    String messageContent = lastMessage != null ? lastMessage.getContent() : "No messages yet";
                    Timestamp messageTime = lastMessage != null ? lastMessage.getEnrolledAt() : null;

                    return new ChatListDTO(chatRoom.getChatRoomId(), chatRoom.getChatRoomName(), messageContent, messageTime);
                                })
                .sorted(Comparator.comparing(ChatListDTO::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

}

