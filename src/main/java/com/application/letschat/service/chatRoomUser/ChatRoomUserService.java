package com.application.letschat.service.chatRoomUser;


import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatRoomUser.ChatRoomUserDTO;
import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.dto.user.UserDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.chatRoomUser.ChatRoomUser;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.chatRoomUser.ChatRoomUserRepository;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomUserService {

    private final ChatRoomUserRepository chatRoomUserRepository;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public void addUserToChatRoom(User user, ChatRoom chatRoom) {
        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setUser(user);
        chatRoomUser.setChatRoom(chatRoom);
        chatRoomUser.setLastReadAt(Timestamp.valueOf(LocalDateTime.now()));
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


    @Transactional
    public void updateLastReadAt(Integer userId) {
        List<ChatRoomUserDTO> chatRoomUserDtos = redisService.getPendingLastReadAt(userId);

        if (chatRoomUserDtos != null && !chatRoomUserDtos.isEmpty()) {
            // Fetch all entities for the user
            List<ChatRoomUser> entities = chatRoomUserRepository.findByUserUserId(userId);
            if (!entities.isEmpty()) {
                // Map DTOs to entities
                Map<String, Timestamp> dtoMap = chatRoomUserDtos.stream()
                        .collect(Collectors.toMap(
                                dto -> dto.getChatRoomId() + ":" + dto.getUserId(),
                                ChatRoomUserDTO::getLastReadAt
                        ));
                entities.forEach(entity -> {
                    String key = entity.getChatRoom().getChatRoomId() + ":" + entity.getUser().getUserId();
                    Timestamp lastReadAt = dtoMap.get(key);
                    if (lastReadAt != null) {
                        entity.setLastReadAt(lastReadAt);
                    }
                });
                chatRoomUserRepository.saveAll(entities);
                redisService.removePendingLastReadAt(userId);
                log.info("Last read at {} has been updated", userId);
            }
        }

    }

    public List<UserDTO> getUsersInChatRoom(Long chatRoomId) {
        return chatRoomUserRepository.findEmailAndUserIdsAndNamesByChatRoomId(chatRoomId);
    }

    public void removeUserFromChat(Long chatRoomId, Integer userId) throws Exception {
        chatRoomUserRepository.deleteByUserIdAndChatRoomId(userId, chatRoomId);
        //system message
        MessageDTO messageDTO = MessageDTO.builder()
                .content(String.format("\"%s\"님이 채팅에서 나갔습니다", userService.getUserById(userId).getName()))
                .chatRoomId(chatRoomId)
                .enrolledAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        redisService.removeChatRoomIdsAndUserIds(userId, chatRoomId);
        redisService.addPendingMessage(messageDTO);
        messagingTemplate.convertAndSend("/topic/private-chat/" + chatRoomId, messageDTO);
    }

    public List<String> getEmailsByChatRoomId(Long chatRoomId) {
        List<UserDTO> users = getUsersInChatRoom(chatRoomId);
        return users.stream()
                .map(UserDTO::getEmail)
                .toList();
    }
}
