package com.application.letschat.service.chatlist;


import com.application.letschat.dto.chatlist.ChatListDTO;
import com.application.letschat.entity.chatroom.ChatRoom;
import com.application.letschat.entity.chatroomuser.ChatRoomUser;
import com.application.letschat.entity.message.Message;
import com.application.letschat.entity.user.User;
import com.application.letschat.repository.chatroomuser.ChatRoomUserRepository;
import com.application.letschat.repository.message.MessageRepository;
import com.application.letschat.repository.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        return chatRoomUsers.stream()
                .map(cru -> {
                    ChatRoom chatRoom = cru.getChatRoom();
                    Message lastMessage = messageRepository.findLastMessageByChatRoom(chatRoom)
                            .orElse(null);
                    String messageContent = lastMessage != null ? lastMessage.getContent() : "새로운 채팅방 (" + sdf.format(chatRoom.getEnrolledAt()) + ")" ;
                    Timestamp messageTime = lastMessage != null ? lastMessage.getEnrolledAt() : null;

                    return new ChatListDTO(chatRoom.getChatRoomId(), chatRoom.getChatRoomName(), messageContent, messageTime, cru.getLastReadAt());
                                })
                .sorted(Comparator.comparing(ChatListDTO::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

}

