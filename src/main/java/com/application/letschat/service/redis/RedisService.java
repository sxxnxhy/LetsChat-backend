package com.application.letschat.service.redis;


import com.application.letschat.controller.message.MessageController;
import com.application.letschat.dto.chatRoomUser.ChatRoomUserDTO;
import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.repository.chatRoomUser.ChatRoomUserRepository;
import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import com.application.letschat.service.notificationService.NotificationService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, MessageDTO> redisTemplate;

    private final RedisTemplate<String, ChatRoomUserDTO> chatRoomUserRedisTemplate;

    private final RedisTemplate<String, Integer> userIdRedisTemplate;

    private final RedisTemplate<String, Long> chatRoomIdRedisTemplate;

    private final UserService userService;

    private final NotificationService notificationService;

    private final ChatRoomUserRepository chatRoomUserRepository;




    public void addPendingMessage(MessageDTO messageDTO) throws Exception {
        String key = "message_queue:" + messageDTO.getChatRoomId();
        redisTemplate.opsForList().rightPush(key, messageDTO); // Add to end

        //toggle refresh to all users of that chatroom that is in the chat list
        notificationService.toggleRefresh(getUserIdsByChatRoomId(messageDTO.getChatRoomId()));

    }

    public List<MessageDTO> getPendingMessages(Long chatRoomId) {
        String key = "message_queue:" + chatRoomId;
        List<MessageDTO> messages = redisTemplate.opsForList().range(key, 0, -1);
        return messages != null ? messages : Collections.emptyList();
    }

    public List<MessageDTO> getAllPendingMessages() {
        try {
            String pattern = "message_queue:*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys.isEmpty()) {
                return Collections.emptyList();
            }
            List<MessageDTO> allMessages = new ArrayList<>();
            for (String key : keys) {
                List<MessageDTO> messages = redisTemplate.opsForList().range(key, 0, -1);
                if (messages != null) {
                    allMessages.addAll(messages);
                }
            }
            return allMessages.isEmpty() ? Collections.emptyList() : allMessages;
        } catch (Exception e) {
            log.error("Failed to fetch all pending messages from Redis", e);
            return Collections.emptyList();
        }
    }

    public void removePendingMessage(Long chatRoomId) {
        String key = "message_queue:" + chatRoomId;
        redisTemplate.delete(key); // Simply delete the entire list for this chat room
    }

    public void removeAllPendingMessages() {
        try {
            String pattern = "message_queue:*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Failed to remove all pending messages from Redis", e);
        }
    }


    public void addPendingLastReadAt(ChatRoomUserDTO chatRoomUserDto) {
        String key = "lastReadTimer:" + chatRoomUserDto.getUserId() + ":" + chatRoomUserDto.getChatRoomId();
        chatRoomUserRedisTemplate.opsForValue().set(key, chatRoomUserDto);
        log.info("마지막 접속 시간" + chatRoomUserDto.getLastReadAt());
        log.info("유저id,챗id:" + chatRoomUserDto.getUserId() + "," + chatRoomUserDto.getChatRoomId());
    }

    public List<ChatRoomUserDTO> getPendingLastReadAt(Integer userId) {
        String pattern = "lastReadTimer:" + userId + ":*";
        List<ChatRoomUserDTO> chatRoomUserDtos = new ArrayList<>();
        try {
            // Get all matching keys
            Set<String> keys = chatRoomUserRedisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                // Fetch all values in one go
                List<ChatRoomUserDTO> dtos = chatRoomUserRedisTemplate.opsForValue().multiGet(keys);
                if (dtos != null) {
                    // Filter out nulls and add to result
                    dtos.stream()
                            .filter(dto -> dto != null)
                            .forEach(chatRoomUserDtos::add);
                }
            }
            return chatRoomUserDtos.isEmpty() ? Collections.emptyList() : chatRoomUserDtos;
        } catch (Exception e) {
            log.error("Failed to fetch pending last read times from Redis for userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    public void removePendingLastReadAt(Integer userId) {
        String pattern = "lastReadTimer:" + userId + ":*";
        try {
            // Get all keys matching the pattern
            Set<String> keys = chatRoomUserRedisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                // Delete all matching keys
                chatRoomUserRedisTemplate.delete(keys);
                log.info("Removed {} pending last read entries for userId: {}", keys.size(), userId);
            }
        } catch (Exception e) {
            log.error("Failed to remove pending last read entries from Redis for userId: {}", userId, e);
        }
    }



    public List<Integer> getUserIdsByChatRoomId(Long chatRoomId) {
        String key = "chatroom_users:" + chatRoomId;
        Set<Integer> userIds = userIdRedisTemplate.opsForSet().members(key);

        if (userIds == null || userIds.isEmpty()) {
            try {
                List<Integer> dbUserIds = chatRoomUserRepository.findUserIdsByChatRoomId(chatRoomId);
                if (!dbUserIds.isEmpty()) {
                    userIdRedisTemplate.opsForSet().add(key, dbUserIds.toArray(new Integer[0]));
                    log.info("Cached {} user IDs for chat room {} in Redis", dbUserIds.size(), chatRoomId);
                    return dbUserIds;
                }
                return Collections.emptyList();
            } catch (Exception e) {
                log.error("Failed to fetch user IDs from DB for chatRoomId: {}", chatRoomId, e);
                return Collections.emptyList();
            }
        }
        return new ArrayList<>(userIds);
    }


    public List<Long> getChatRoomIdsByUserId(Integer userId) {
        String key = "user_chatrooms:" + userId;
        Set<Long> chatRoomIds = chatRoomIdRedisTemplate.opsForSet().members(key);

        // If not found in Redis, fetch from the database and cache it
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            try {
                // Fetch chat room IDs from the repository (assumes this method exists or can be added)
                List<Long> dbChatRoomIds = chatRoomUserRepository.findChatRoomIdsByUserId(userId);
                if (!dbChatRoomIds.isEmpty()) {
                    // Cache the result in Redis
                    chatRoomIdRedisTemplate.opsForSet().add(key, dbChatRoomIds.toArray(new Long[0]));
                    log.info("Cached {} chat room IDs for user {} in Redis", dbChatRoomIds.size(), userId);
                    return dbChatRoomIds;
                }
                return Collections.emptyList();
            } catch (Exception e) {
                log.error("Failed to fetch chat room IDs from DB for userId: {}", userId, e);
                return Collections.emptyList();
            }
        }
        return new ArrayList<>(chatRoomIds);
    }

    public void addChatRoomIdsAndUserIds(Integer userId, Long chatRoomId) {
        // Add chatRoomId to user's set
        String keyForUserId = "user_chatrooms:" + userId;
        chatRoomIdRedisTemplate.opsForSet().add(keyForUserId, chatRoomId);
        log.info("Added chatRoomId {} to user {} in Redis", chatRoomId, userId);

        // Add userId to chat room's set
        String keyForChatRoomId = "chatroom_users:" + chatRoomId;
        userIdRedisTemplate.opsForSet().add(keyForChatRoomId, userId);
        log.info("Added userId {} to chatRoomId {} in Redis", userId, chatRoomId);
    }





}
