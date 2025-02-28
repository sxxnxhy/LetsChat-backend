package com.application.letschat.service.redis;


import com.application.letschat.dto.chatRoomUser.ChatRoomUserDTO;
import com.application.letschat.dto.message.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, MessageDTO> redisTemplate;

    private final RedisTemplate<String, ChatRoomUserDTO> chatRoomUserRedisTemplate;


    public void addPendingMessage(MessageDTO messageDTO) throws Exception {
        redisTemplate.opsForList().rightPush("message_queue", messageDTO); // Add to end
    }

    public List<MessageDTO> getPendingMessages(Long chatRoomId) {
        List<MessageDTO> messages = redisTemplate.opsForList().range("message_queue", 0, -1);
        return messages.stream()
                .filter(m -> m.getChatRoomId().equals(chatRoomId))
                .toList();
    }

    public List<MessageDTO> getAllPendingMessages() {
        try {
            List<MessageDTO> messages = redisTemplate.opsForList().range("message_queue", 0, -1);
            return messages != null ? messages : Collections.emptyList();
        } catch (Exception e) {
            // Log the error and return an empty list to prevent the scheduler from crashing
            log.error("Failed to fetch pending messages from Redis", e);
            return Collections.emptyList();
        }
    }


    public void removePendingMessage(Long chatRoomId) {
        List<MessageDTO> messages = redisTemplate.opsForList().range("message_queue", 0, -1);
        if (messages != null) {
            redisTemplate.delete("message_queue"); // Clear the list
            messages.stream()
                    .filter(m -> !m.getChatRoomId().equals(chatRoomId))
                    .forEach(m -> redisTemplate.opsForList().rightPush("message_queue", m));
        }
    }

    public void removeAllPendingMessages() {
        redisTemplate.delete("message_queue");
    }


    public void addPendingLastReadAt(ChatRoomUserDTO chatRoomUserDto) {
        String key = "lastReadTimer:" + chatRoomUserDto.getUserId() + ":" + chatRoomUserDto.getChatRoomId();
        chatRoomUserRedisTemplate.opsForValue().set(key, chatRoomUserDto);
        log.info("마지막 접속 시간" + chatRoomUserDto.getLastReadAt());
        log.info("유저id,챗id:" + chatRoomUserDto.getUserId() + "," + chatRoomUserDto.getChatRoomId());
    }

//    public List<ChatRoomUserDTO> getPendingLastReadAt(Integer userId, Integer chatRoomId) {
//        String key = "lastReadTimer:" + userId + ":" + chatRoomId;
//        List<ChatRoomUserDTO> chatRoomUserDtos = chatRoomUserRedisTemplate.opsForList().range(key, 0, -1);
//        try{
//            return chatRoomUserDtos != null ? chatRoomUserDtos : Collections.emptyList();
//        } catch (Exception e) {
//            log.error("Failed to fetch pending messages from Redis", e);
//            return Collections.emptyList();
//        }
//    }
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


}
