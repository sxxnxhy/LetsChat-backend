package com.application.letschat.service.redis;


import com.application.letschat.dto.message.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, MessageDTO> redisTemplate;


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


}
